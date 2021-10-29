package com.google.adapter.configuration.lucene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adapter.exceptions.NullParameterException;

import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class LuceneIndexBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneIndexBuilder.class);
    private static final String INDEXED_FILE_PATH = System.getProperty("user.home") + "/.lucene";

    private static LuceneIndexBuilder builder;
    private FSDirectory directory;
    private Directory inMemoryDirectory;
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private IndexReader indexReader;
    private ObjectMapper mapper = new ObjectMapper();

    private int numberOfAvailableProcessors = 0;

    private LuceneIndexBuilder() {
    }

    public static LuceneIndexBuilder getInstance(Analyzer analyzer) throws IOException {
        if (Objects.nonNull(builder)) {
            return builder;
        }

        return (builder = new LuceneIndexBuilder().createIndexWriter(analyzer));
    }

    /**
     * This method creates {@link IndexWriter}
     *
     * @param analyzer
     * @return Instance of LuceneIndexBuilder
     * @throws IOException
     */
    private LuceneIndexBuilder createIndexWriter(Analyzer analyzer) throws IOException {
        LOGGER.info("Lucene index writer creation started");

        if (new File(INDEXED_FILE_PATH).isDirectory()) {
            new File(INDEXED_FILE_PATH).deleteOnExit();
        }

        this.analyzer = analyzer;
        directory = FSDirectory.open(Paths.get(INDEXED_FILE_PATH));
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        this.indexWriter = new IndexWriter(directory, indexWriterConfig);
        this.numberOfAvailableProcessors = Runtime.getRuntime().availableProcessors();
        LOGGER.info("Lucene index writer creation done at path {}", INDEXED_FILE_PATH);
        return this;
    }


    /**
     *
     * @param contentList List of items to be indexed
     * @param keyPrefix Prefix value of key
     * @param rebuildIndexes If true then creates new IndexWriter, if false then utilize the
     *                       previously created one.
     * @param <T> Generic type
     * @throws IOException IOException
     */
    public <T> void addContentToIndex(List<T> contentList, LuceneKey keyPrefix, boolean rebuildIndexes)
            throws IOException {

        LOGGER.info(
                "LuceneIndexBuilder -> addContentToIndex started execution with rebuildIndexes as "
                        + rebuildIndexes);
        if (Objects.isNull(contentList)) {
            throw new NullParameterException(
                    "LuceneIndexBuilder -> addContentToIndex -> RFCMethod list is null");
        }

        if (rebuildIndexes && Objects.nonNull(this.indexWriter) && this.indexWriter.isOpen()) {
            this.indexWriter.deleteAll();
        }

        if (rebuildIndexes && Objects.nonNull(this.indexReader)) {
            this.indexReader.close();
        }

        this.indexWriter = getIndexWriter(rebuildIndexes);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfAvailableProcessors);
        executor.submit(
                () -> {
                    try {
                        synchronized (this.directory) {
                            this.buildIndex(contentList, keyPrefix.getValue());
                            this.indexReader = null;
                            Runtime.getRuntime().gc();
                            this.indexReader = DirectoryReader.open(new RAMDirectory(directory, IOContext.READONCE));
                        }
                    } catch (IOException e) {
                        LOGGER.error("LuceneIndexBuilder -> addContentToIndex -> ", e);
                    }
                });
        executor.shutdown();

        LOGGER.info("LuceneIndexBuilder -> addContentToIndex done with execution");
    }

    /**
     * This method search passed queryString in whole lucene index and if matched it returns {@link
     * Document} for the same
     * @param queryString  String value
     * @param topResults Result value
     * @param clazz To decide class name on runtime
     * @param <T> Generic type
     * @return List of Document
     * @throws IOException IOException
     * @throws ParseException ParseException
     */
    public <T> List<T> search(String queryString, int topResults, Class<T> clazz) throws IOException, ParseException {
        List<Document> searchList = new ArrayList<>();
        Object[] objects = null;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfAvailableProcessors);
        synchronized (this.directory) {
            Runtime.getRuntime().gc();
            IndexSearcher searcher = new IndexSearcher(this.indexReader, executorService);
            objects = prepareQuery(queryString);
            try {
                TopDocs topDocs = searcher.search((Query) objects[0], topResults, Sort.RELEVANCE);
                if (Objects.nonNull(topDocs.scoreDocs) && topDocs.scoreDocs.length > 0) {
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    for (ScoreDoc scoreDoc : scoreDocs) {
                        searchList.add(searcher.doc(scoreDoc.doc));
                    }
                }
            } catch (IOException e) {
                LOGGER.error("LuceneIndexBuilder -> search -> ", e);
            }
        }
        List<T> convertedList = convert(searchList, (String) objects[1], clazz);
        searchList = null;
        Runtime.getRuntime().gc();
        return convertedList;
    }

    /**
     * This method converts RFCMethod JSON to Instance of RFCMethod
     * @param documentList List of Document
     * @param key Key value
     * @param clazz To decide class name on runtime
     * @param <T> Generic type
     * @return Converted list
     * @throws IOException IOException
     */
    public <T> List<T> convert(List<Document> documentList, String key, Class<T> clazz) throws IOException {
        List<T> objectArrayList = new ArrayList<>();
        for (Document document : documentList) {
            objectArrayList.add(mapper.readValue(document.get(key), clazz));
        }
        return objectArrayList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * ****************************** Private method section ******************************
     */
    private Object[] prepareQuery(String queryString) throws ParseException {

        try {
            String key = queryString.substring(0, queryString.lastIndexOf("____"));
            String text = queryString.split("____")[3].replaceAll(":", "")
                    .replaceAll("\\(", "").replaceAll("\\)", "")
                    .trim();

            QueryParser parser = new QueryParser(key, analyzer);
            StringBuilder builder = new StringBuilder();
            String[] contentArray = text.split(" ");
            builder.append("+").append(key).append(":").append(contentArray[0]);
            if (contentArray.length > 0) {
                for (String searchContent : contentArray) {
                    builder.append(" +").append(searchContent).append("~");
                }
            }

            Query query = parser.parse(builder.toString());
            Object[] objects = {query, key};
            return objects;
        } catch (Exception exception) {
            throw exception;
        }
    }

    private void buildIndex(List<?> objectList, String keyPrefix) throws IOException {
        for (Object content : objectList) {
            addDocument(keyPrefix, content);
        }

        this.indexWriter.commit();
        this.flush();
    }

    private void flush() throws IOException {
        this.indexWriter.close();
    }

    private void addDocument(String keyPrefix, Object object) throws IOException {
        String jsonObject = mapper.writeValueAsString(object);
        Document document = new Document();
        document.add(new TextField("any____content____" + keyPrefix, jsonObject, Field.Store.YES));
        this.indexWriter.addDocument(document);
    }

    private IndexWriter getIndexWriter(boolean isNewInstanceNeeded) throws IOException {

        if (isNewInstanceNeeded) {
            this.indexWriter.close();
            this.directory.close();
            this.directory = null;
        }
        return isNewInstanceNeeded
                ? createIndexWriter(analyzer).indexWriter
                : this.indexWriter.isOpen() ? this.indexWriter : createIndexWriter(analyzer).indexWriter;
    }
}
