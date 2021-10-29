package com.google.adapter.configuration.lucene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adapter.exceptions.NullParameterException;
import com.google.adapter.exceptions.SystemException;
import com.google.adapter.model.RFCMethod;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LuceneIndexBuilderTest {

    private static List<RFCMethod> rfcMethods;
    private static LuceneIndexBuilder luceneIndexBuilder;
    private LuceneIndexBuilder indexBuilder = spy(luceneIndexBuilder);

    @BeforeAll
    static void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            rfcMethods = Arrays.asList(mapper.readValue(new File("src/test/resources/sapData.txt"), RFCMethod[].class));
            luceneIndexBuilder = LuceneIndexBuilder.getInstance(new StandardAnalyzer());
        } catch (IOException e) {
            throw SystemException.throwException(e.getMessage(), e);
        }
    }

    @Test
    void testAddContentToIndex() throws IOException {

        Assertions.assertThrows(NullParameterException.class, () -> {
            indexBuilder.addContentToIndex(null, LuceneKey.RFC, false);
        });

        doThrow(IOException.class).when(indexBuilder).addContentToIndex(anyList(), any(), anyBoolean());
        try {
            indexBuilder.addContentToIndex(anyList(), any(), anyBoolean());
        } catch (IOException ioe) {
            Assertions.assertSame(ioe.getClass(), IOException.class);
        }

        doNothing().when(indexBuilder).addContentToIndex(anyList(), any(), anyBoolean());
        indexBuilder.addContentToIndex(anyList(), any(), anyBoolean());

        verify(indexBuilder, times(2)).addContentToIndex(anyList(), any(), anyBoolean());
    }

    /*@Test
    void testSearch() throws IOException, ParseException, InterruptedException {

        indexBuilder.addContentToIndex(rfcMethods, LuceneKey.RFC, false);
        Thread.sleep(5000L);

        doThrow(IOException.class).when(indexBuilder).search(anyString(), anyInt(), any());
        try {
            indexBuilder.search(anyString(), anyInt(), any());
        } catch (IOException ioe) {
            Assertions.assertSame(ioe.getClass(), IOException.class);
        }

        doThrow(ParseException.class).when(indexBuilder).search(anyString(), anyInt(), any());
        try {
            indexBuilder.search(anyString(), anyInt(), any());
        } catch (ParseException parseException) {
            Assertions.assertSame(parseException.getClass(), ParseException.class);
        }

        reset(indexBuilder);

        List<RFCMethod> rfcMethodList = indexBuilder.search(LuceneKey.RFC.any("ENQUEUE_E_CMDEF_SGLE"),
                10, RFCMethod.class);

        MatcherAssert.assertThat(rfcMethodList, Matchers.hasSize(1));
        Assertions.assertTrue(rfcMethods.contains(rfcMethodList.get(0)));

        verify(indexBuilder, times(1)).search(LuceneKey.RFC.any("ENQUEUE_E_CMDEF_SGLE"),
                10, RFCMethod.class);
    }*/
}