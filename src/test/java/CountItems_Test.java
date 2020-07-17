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
public class CountItems_Test {
    public static final String DATASET_RESOURCE_PATH = "/CountItems_data.json";
    public static final String CONFIGURATION_RESOURCE_PATH = "/CountItems_config.json";


    public static void main(String[] args) {
        CountItems_Test context = new CountItems_Test();
        ObjectMapper mapper = new ObjectMapper();
        // Prettify the output
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            //Load input OpenLAPDataSet
            /*
                This OpenLAPDataSet does not have the same input 'ids' as the one you defined in the constructor of your implementation.
                But the number of required columns that you want as input should be available in this sample dataset.
                Also the type of the columns available in this OpenLAPDataSet should be the same as you want.
            */

            OpenLAPDataSet inputDataSet = mapper.readValue(context.loadResourceFile(DATASET_RESOURCE_PATH), OpenLAPDataSet.class);

            //Load configuration
            /*
                The next step is to define the OLAPPortConfiguration, whose purpose is to tell which column
                of the incoming OpenLAPDataSet should go into which column of the expecting OpenLAPDataSet of the implemented
                analytics method.

                E.g. the OpenLAPDataSet loaded above have the following configuration

                    "type":"Text",
                    "id":"raw_data",
                    "required":true,
                    "title":"Items List",
                    "description":"List of items"

                The input OpenLAPDataSet the ItemCounterImplementation class expecting which is defined in the constructor is

                    "type":"Text",
                    "id":"item_name",
                    "required":true,
                    "title":"Items",
                    "description":"List of items to count"

                In the Mapping the column of the incoming OpenLAPDataSet are named outputPort since they are output of
                some raw data and the expected input. E.g.

                    {
                        "mapping":[
                            {
                                "outputPort":{
                                    "type":"Text",
                                    "id":"raw_data",
                                    "required":true,
                                    "title":"Items List",
                                    "description":"List of items"
                                },
                                "inputPort":{
                                    "type":"Text",
                                    "id":"item_name",
                                    "required":true,
                                    "title":"Items",
                                    "description":"List of items to count"
                                }
                            }
                        ]
                    }

                If you are expecting more or less columns in the input OpenLAPDataSet defined in the constructor
                than you have to update the CountItems_data.json to add or remove columns with configuration and expected
                data (the data type should be the same as the type defined in the configuration)
                and update the OLAPPortConfiguration in CountItems_config.json file to incorporate new mappings.

                 E.g.
                {
                        "mapping":[
                            {
                                "outputPort":{
                                    "type":"Text",
                                    "id":"raw_data",
                                    "required":true,
                                    "title":"Items List",
                                    "description":"List of items"
                                },
                                "inputPort":{
                                    "type":"Text",
                                    "id":"item_name",
                                    "required":true,
                                    "title":"Items",
                                    "description":"List of items to count"
                                }
                            },
                            {
                                "outputPort":{
                                    "type":"Numeric",
                                    "id":"raw_data2",
                                    "required":true,
                                    "title":"Items List",
                                    "description":"List of items"
                                },
                                "inputPort":{
                                    "type":"Numeric",
                                    "id":"expecting_column_2",
                                    "required":true,
                                    "title":"Column 2",
                                    "description":"This is integer column in the input OpenLAPDataSet defined in the constructor"
                                }
                            }
                        ]
                    }
            */
            OpenLAPPortConfig inputConfiguration = mapper.readValue(context.loadResourceFile(CONFIGURATION_RESOURCE_PATH), OpenLAPPortConfig.class);

            //Instantiate the implemented analytics method
            AnalyticsMethod method = new CountItems();

            // Add parameters and initialize it
            Map<String, String> params = new HashMap<String, String>();
            params.put("return_count", "" + 10);
            params.put("count_direction", "Least Occurring");

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
