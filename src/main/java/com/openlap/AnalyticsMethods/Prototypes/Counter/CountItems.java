package com.openlap.AnalyticsMethods.Prototypes.Counter;

import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dynamicparam.OpenLAPDynamicParamDataType;
import com.openlap.dynamicparam.OpenLAPDynamicParamFactory;
import com.openlap.dynamicparam.OpenLAPDynamicParamType;
import com.openlap.dynamicparam.OpenLAPDynamicParams;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.OpenLAPDynamicParamException;
import com.openlap.template.AnalyticsMethod;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Arham Muslim on 11-Mar-16.
 * This Analytics Method counts the occurrences of each item in the incoming OpenLAP-DataSet, sort them in descending order and select the top 10 items
 */
public class CountItems extends AnalyticsMethod {
    public CountItems() {
        this.setInput(new OpenLAPDataSet());
        this.setOutput(new OpenLAPDataSet());
        this.setParams(new OpenLAPDynamicParams());

        try {
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_name", OpenLAPColumnDataType.Text, true, "Items", "List of items to count")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("users", OpenLAPColumnDataType.Text, false, "User", "List of users for whom items should be counted if [Unique items] count type is selected.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_name", OpenLAPColumnDataType.Text, true, "Item Names", "List of top most occurring items in the list")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_count", OpenLAPColumnDataType.Numeric, true, "Item Count", "Number of time each item occurred in the list")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("count_direction", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Counting direction", "Count most occurring items of least occurring items", "Most Occurring", "Most Occurring,Least Occurring", true)
            );
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("return_count", OpenLAPDynamicParamType.Textbox, OpenLAPDynamicParamDataType.INTEGER, "Number of items to return (N)", "Specify the number of items that need to be returned. e.g. 10 will return top 10 items. -1 will return all items.", 10, "", true)
            );
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("search_type", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Counting type", "Count all item occurrences per user or only single item per user. For [Unique items] count type, the [User] column is required", "All items", "All items,Unique items", true)
            );
        } catch (OpenLAPDynamicParamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Count N most occurring or least occurring items";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Count, sort and return N most occurring or least occurring items in the list.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    @Override
    protected void implementationExecution() {
        try {

            String search_type = (String)this.getParams().getParams().get("search_type").getValue();
            int counter = (Integer) this.getParams().getParams().get("return_count").getValue();
            String count_direction = (String)this.getParams().getParams().get("count_direction").getValue();

            if(search_type.equals("All items")) {

                LinkedHashMap<String, Integer> itemCount = new LinkedHashMap<String, Integer>();
                // Iterate over each word of the column of the arrays
                for (Object word : this.getInput().getColumns().get("item_name").getData()) {
                    if (itemCount.containsKey(word))
                        itemCount.put((String) word, itemCount.get((String) word) + 1);
                    else
                        itemCount.put((String) word, 1);
                }
                Set<Map.Entry<String, Integer>> itemCountSet = itemCount.entrySet();

                if (itemCountSet.size() < counter || counter == -1)
                    counter = itemCountSet.size();

                for (; counter > 0; counter--) {

                    Iterator<Map.Entry<String, Integer>> itemCountSetIterator = itemCountSet.iterator();

                    Map.Entry<String, Integer> selectedEntry = itemCountSetIterator.next();

                    while (itemCountSetIterator.hasNext()) {
                        Map.Entry<String, Integer> curEntry = itemCountSetIterator.next();

                        if(count_direction.equals("Most Occurring")){
                            if (curEntry.getValue() > selectedEntry.getValue())
                                selectedEntry = curEntry;
                        }
                        else{
                            if (curEntry.getValue() < selectedEntry.getValue())
                                selectedEntry = curEntry;
                        }
                    }
                    getOutput().getColumns().get("item_name").getData().add(selectedEntry.getKey());
                    getOutput().getColumns().get("item_count").getData().add(selectedEntry.getValue());
                    itemCountSet.remove(selectedEntry);
                }
            }
            else{
                LinkedHashMap<String, List<String>> itemCount = new LinkedHashMap<String, List<String>>();

                Iterator items = this.getInput().getColumns().get("item_name").getData().iterator();
                Iterator users = this.getInput().getColumns().get("users").getData().iterator();

                while (items.hasNext() && users.hasNext()) {
                    String item = (String) items.next();
                    String user = (String) users.next();

                    if (itemCount.containsKey(item)) {
                        List<String> usersList = itemCount.get(item);

                        if(!usersList.contains(user))
                            usersList.add(user);
                    } else {
                        List<String> newUsersList = new ArrayList<String>();
                        newUsersList.add(user);
                        itemCount.put(item, newUsersList);
                    }
                }

                Set<Map.Entry<String, List<String>>> itemCountSet = itemCount.entrySet();

                if (itemCountSet.size() < counter || counter == -1)
                    counter = itemCountSet.size();

                for (; counter > 0; counter--) {

                    Iterator<Map.Entry<String, List<String>>> itemCountSetIterator = itemCountSet.iterator();

                    Map.Entry<String, List<String>> selectedEntry = itemCountSetIterator.next();

                    while (itemCountSetIterator.hasNext()) {
                        Map.Entry<String, List<String>> curEntry = itemCountSetIterator.next();

                        if(count_direction.equals("Most Occurring")){
                            if (curEntry.getValue().size() > selectedEntry.getValue().size())
                                selectedEntry = curEntry;
                        }
                        else{
                            if (curEntry.getValue().size() < selectedEntry.getValue().size())
                                selectedEntry = curEntry;
                        }

                    }
                    getOutput().getColumns().get("item_name").getData().add(selectedEntry.getKey());
                    getOutput().getColumns().get("item_count").getData().add(selectedEntry.getValue().size());
                    itemCountSet.remove(selectedEntry);
                }
            }
        }
        catch (Exception exc)
        {
            System.out.println(exc.getMessage());
        }
    }
    
    @Override
    public InputStream getPMMLInputStream() {
        return null;
    }

    @Override
    public Boolean hasPMML() {
        return false;
    }
}
