package com.google.adapter.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adapter.constants.AdapterConstants;
import com.google.adapter.exceptions.FormatParsing610Exception;
import com.google.adapter.exceptions.NullParameterException;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dhiraj.kumar
 */
public class ValidationParameter {

    private String errorMessage = "";
    private String[] mandatoryParameters = {""};

    public static boolean validateProgramID(JsonNode jsonNode, Properties properties) {
        boolean isProgramIdAvailable = false;
        if (null == jsonNode || null == jsonNode.get("DATA")) {
            return isProgramIdAvailable;
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Data> dataList = Arrays.asList(mapper.convertValue(jsonNode.get("DATA"), Data[].class));
        String string = JSONReader.filterData(dataList, Data.class, new String[]{"G=10.132.0.4", "G=10.132.0.8"}).stream().map(object -> object.getKey().split(";")[0].trim())
                .collect(Collectors.joining(","));

        /*ReadPropertyFile readPropertyFile = new ReadPropertyFile("Google_SAP_Connection.properties");
        Properties properties = readPropertyFile.readPropertyFile();*/
        isProgramIdAvailable = string.toLowerCase().contains(((String) properties.get("jco.server.progid")).trim().toLowerCase());

        return isProgramIdAvailable;
    }

/*    public static void main(String[] args) {
        ValidationParameter parameter = new ValidationParameter();
        Properties p = new Properties();
        p.put(DestinationDataProvider.JCO_GROUP, "SDKF");
        p.put(DestinationDataProvider.JCO_MSHOST, "10.2.3.4");
        System.out.println(parameter.validateLoadBalancer(p));
        System.out.println(parameter.getErrorMessage());
    }*/

    /**
     * @param properties This parameter is to get the connection value that is provided for the
     *                   connection
     * @return Boolean value according to the match
     * @throws FormatParsing610Exception This is the exception class if the parameter is not compliant
     *                                   to the pattern
     */
    public boolean validate(Properties properties) {
        if (properties.get(DestinationDataProvider.JCO_ASHOST) != null) {
            String ip = (String) properties.get(DestinationDataProvider.JCO_ASHOST);
            Pattern pattern = Pattern.compile(AdapterConstants.IPADDRESS_PATTERN);
            Matcher matcher = pattern.matcher(ip);
            boolean isMatched = matcher.matches();
            this.errorMessage = !isMatched ? ("Please provide a valid value for " + DestinationDataProvider.JCO_ASHOST +
                    "(current value: " + ip + ")") : DestinationDataProvider.JCO_ASHOST;
            return isMatched;
        } else {
            this.errorMessage = "('" + DestinationDataProvider.JCO_ASHOST + "') is missing";
        }
        return true;
    }

    /**
     * @param properties This will provide the parameter to implement check
     * @return Boolean value accordind to the null constraint check
     * @throws NullParameterException This method is to check that parameter which are passed should
     *                                not be blank of null
     */
    public boolean notNullConstraint(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getValue().toString().isEmpty() || entry.getValue().toString().equals("null")) {
                this.errorMessage = entry.getKey() + " is either blank or null.";
                return false;
            }
        }
        return checkLang(properties);
    }

    /**
     * Check whether language argument contains two alphabets
     *
     * @param properties List of Parameters
     * @return true if it passes validation
     */
    public boolean checkLang(Properties properties) {
        if (properties.get(DestinationDataProvider.JCO_LANG) != null) {
            String ip = (String) properties.get(DestinationDataProvider.JCO_LANG);
            Pattern pattern = Pattern.compile(AdapterConstants.LANGUAGE_PATTERN);
            Matcher matcher = pattern.matcher(ip);
            boolean isMatched = matcher.matches();
            this.errorMessage = !isMatched ? ("Please provide a valid value for " + DestinationDataProvider.JCO_LANG +
                    "(current value: " + ip + ")") : DestinationDataProvider.JCO_LANG;
            return isMatched;
        } else {
            this.errorMessage = "('" + DestinationDataProvider.JCO_LANG + "') is missing";
        }
        return true;
    }

    /**
     * Check whether language argument contains two alphabets
     *
     * @param properties List of Parameters
     * @return true if it passes validation
     */
    public boolean checkClient(Properties properties) {
        if (properties.get(DestinationDataProvider.JCO_CLIENT) != null) {
            String ip = (String) properties.get(DestinationDataProvider.JCO_CLIENT);
            Pattern pattern = Pattern.compile(AdapterConstants.CLIENT_PATTERN);
            Matcher matcher = pattern.matcher(ip);
            boolean isMatched = matcher.matches();
            this.errorMessage = !isMatched ? ("Please provide a valid value for " + DestinationDataProvider.JCO_CLIENT +
                    "(current value: " + ip + ")") : DestinationDataProvider.JCO_CLIENT;
            return isMatched;
        } else {
            this.errorMessage = "('" + DestinationDataProvider.JCO_CLIENT + "') is missing";
        }
        return true;
    }

    /**
     * Check whether Pool Capacity argument contains not more then three digits
     *
     * @param properties List of Parameters
     * @return true if it passes validation
     */
    public boolean checkPoolCapacity(Properties properties) {
        if (properties.get(DestinationDataProvider.JCO_POOL_CAPACITY) != null) {
            String ip = (String) properties.get(DestinationDataProvider.JCO_POOL_CAPACITY);
            Pattern pattern = Pattern.compile(AdapterConstants.NON_MANDAT_PARAM_PATTERN);
            Matcher matcher = pattern.matcher(ip);
            boolean isMatched = matcher.matches();
            this.errorMessage = !isMatched ? ("Please provide a valid value for " + DestinationDataProvider.JCO_POOL_CAPACITY +
                    "(current value: " + ip + ")") : DestinationDataProvider.JCO_POOL_CAPACITY;
            return isMatched;
        } else {
            this.errorMessage = "('" + DestinationDataProvider.JCO_POOL_CAPACITY + "') is missing";
        }
        return true;
    }

    /**
     * Check whether Peak Limit argument contains not more then three digits
     *
     * @param properties List of Parameters
     * @return true if it passes validation
     */
    public boolean checkPeakLimit(Properties properties) {
        if (properties.get(DestinationDataProvider.JCO_PEAK_LIMIT) != null) {
            String ip = (String) properties.get(DestinationDataProvider.JCO_PEAK_LIMIT);
            Pattern pattern = Pattern.compile(AdapterConstants.NON_MANDAT_PARAM_PATTERN);
            Matcher matcher = pattern.matcher(ip);
            boolean isMatched = matcher.matches();
            this.errorMessage = !isMatched ? ("Please provide a valid value for " + DestinationDataProvider.JCO_PEAK_LIMIT +
                    "(current value: " + ip + ")") : DestinationDataProvider.JCO_PEAK_LIMIT;
            return isMatched;
        } else {
            this.errorMessage = "('" + DestinationDataProvider.JCO_PEAK_LIMIT + "') is missing";
        }
        return true;
    }

    public boolean validateLoadBalancer(Properties properties) {
        boolean isValidLBDetails = false;
        String[] loadBalancerRequest = {DestinationDataProvider.JCO_GROUP, DestinationDataProvider.JCO_MSHOST};
        if (Stream.of(loadBalancerRequest).noneMatch(loadbalance -> properties.containsKey(loadbalance))) {
            return true;
        }

        StringBuilder builder = new StringBuilder();
        isValidLBDetails = Stream.of(loadBalancerRequest).allMatch(loadbalance -> {
            if (!properties.containsKey(loadbalance)) {
                builder.append("('").append(loadbalance).append("') is missing.");
                this.errorMessage = "('" + loadbalance + "') is missing.";
                return false;
            }
            if (properties.get(DestinationDataProvider.JCO_MSHOST) != null) {
                String ip = (String) properties.get(DestinationDataProvider.JCO_MSHOST);
                Pattern pattern = Pattern.compile(AdapterConstants.IPADDRESS_PATTERN);
                Matcher matcher = pattern.matcher(ip);
                boolean isMatched = matcher.matches();
                this.errorMessage = !isMatched ? ("Please provide a valid value for " + DestinationDataProvider.JCO_MSHOST +
                        "(current value: " + ip + ")") : DestinationDataProvider.JCO_MSHOST;
                return isMatched;
            }
            return true;
        });
        return isValidLBDetails;
    }

    /**
     * This method validates connection data for SNC Client
     *
     * @param properties Instance of SAPProperties
     * @return true if all criteria are good to go else false
     */
    public boolean checkSNC(Properties properties) {
        boolean isValidSNCDetails;
        String[] sncKeys = {DestinationDataProvider.JCO_SNC_MODE, DestinationDataProvider.JCO_SNC_QOP,
                DestinationDataProvider.JCO_SNC_LIBRARY, DestinationDataProvider.JCO_SNC_MYNAME,
                DestinationDataProvider.JCO_SNC_PARTNERNAME};

        if (Stream.of(sncKeys).noneMatch(snc -> properties.containsKey(snc))) {
            return true;
        }

        StringBuilder builder = new StringBuilder();
        isValidSNCDetails = Stream.of(sncKeys).allMatch(snc -> {
            if (!properties.containsKey(snc)) {
                builder.append("('").append(snc).append("') is missing.");
                return false;
            }
            return true;
        });

        if (!isValidSNCDetails) {
            this.errorMessage = builder.toString();
        }

        if (isValidSNCDetails) {
            isValidSNCDetails = !(Stream.of(sncKeys).filter(snc -> (Objects.isNull(properties.get(snc)) || properties.getProperty(snc).isEmpty())).findAny().isPresent());
            if (!isValidSNCDetails) {
                return false;
            }
            String sncMode = (String) properties.get(DestinationDataProvider.JCO_SNC_MODE);
            String sncQop = (String) properties.get(DestinationDataProvider.JCO_SNC_QOP);
            if (!("0".equalsIgnoreCase(sncMode) || "1".equalsIgnoreCase(sncMode))) {
                this.errorMessage = "Wrong value provided for SNC Mode[" + sncMode + "], SNC Mode values can be either 0 or 1.";
                isValidSNCDetails = false;
            }

            if (!(AdapterConstants.SNC_QOP.indexOf(sncQop) > -1)) {
                this.errorMessage = "Wrong value provided for SNC Qop[" + sncQop + "]";
                isValidSNCDetails = false;
            }

            String libraryPath = (String) properties.get(DestinationDataProvider.JCO_SNC_LIBRARY);
            if (!(new File(libraryPath).exists())) {
                this.errorMessage = "SNC Library file does not exist on given path [" + libraryPath + "]";
                isValidSNCDetails = false;
            }
        }
        return isValidSNCDetails;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    static class Data implements Serializable {
        @JsonProperty("WA")
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

}
