import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.openlap.AnalyticsMethods.Prototypes.Counter.CountItems;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dataset.OpenLAPPortConfig;
import com.openlap.exceptions.AnalyticsMethodInitializationException;
import com.openlap.template.AnalyticsMethod;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arham Muslim
 * on 23-Mar-16.
 */
public class TimeBased_Test {
    public static final String DATASET_RESOURCE_PATH = "/TimeBased_data.json";
    public static final String CONFIGURATION_RESOURCE_PATH = "/TimeBased_config.json";


    public static void main(String[] args) {
        TimeBased_Test context = new TimeBased_Test();
        ObjectMapper mapper = new ObjectMapper();
        // Prettify the output
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            //Load input OpenLAPDataSet
            OpenLAPDataSet inputDataSet = mapper.readValue(context.loadResourceFile(DATASET_RESOURCE_PATH), OpenLAPDataSet.class);

            //Load configuration
            OpenLAPPortConfig inputConfiguration = mapper.readValue(context.loadResourceFile(CONFIGURATION_RESOURCE_PATH), OpenLAPPortConfig.class);

            //Instantiate the implemented analytics method
            AnalyticsMethod method = new CountItems();

            // Add parameters and initialize it
            Map<String, String> params = new HashMap<String, String>();
            params.put("time_period", "Day");

            //Initialize with the input OpenLAPDataSet and configuration
            method.initialize(inputDataSet, inputConfiguration, params);

            //Execute the WikiWordCounterAnalyticsMethod
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

    InputStream loadResourceFile(String configurationResourcePath) {
        return this.getClass().getResourceAsStream(configurationResourcePath);
    }

}
