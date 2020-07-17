import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.openlap.AnalyticsMethods.Prototypes.Assignment.AverageTime;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dataset.OpenLAPPortConfig;
import com.openlap.exceptions.AnalyticsMethodInitializationException;
import com.openlap.template.AnalyticsMethod;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Arham Muslim
 * on 23-Mar-16.
 */
public class AverageTime_Test {
    public static final String DATASET_RESOURCE_PATH = "/AverageTime_data.json";
    public static final String CONFIGURATION_RESOURCE_PATH = "/AverageTime_config.json";


    public static void main(String[] args) {
        AverageTime_Test context = new AverageTime_Test();
        ObjectMapper mapper = new ObjectMapper();
        // Prettify the output
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            //Loading the OpenLAP DataSet from AverageTime_data.json
            OpenLAPDataSet inputDataSet = mapper.readValue(context.loadResourceFile(DATASET_RESOURCE_PATH), OpenLAPDataSet.class);

            //Loading the OpenLAP Port configuration from AverageTime_config.json
            OpenLAPPortConfig inputConfiguration = mapper.readValue(context.loadResourceFile(CONFIGURATION_RESOURCE_PATH), OpenLAPPortConfig.class);

            //Instantiate the implemented analytics method
            AnalyticsMethod method = new AverageTime();

            //Initialize with the input OpenLAPDataSet and configuration
            method.initialize(inputDataSet,inputConfiguration);

            //Execute the Analytics Method
            method.execute();

            //Output the result in console
            System.out.println("Output of the test:");
            System.out.println(mapper.writeValueAsString(method.getOutput()));
        } catch (AnalyticsMethodInitializationException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream loadResourceFile(String configurationResourcePath) {
        return this.getClass().getResourceAsStream(configurationResourcePath);
    }

}
