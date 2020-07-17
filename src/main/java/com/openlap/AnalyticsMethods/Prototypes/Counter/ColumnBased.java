package com.openlap.AnalyticsMethods.Prototypes.Counter;

import com.openlap.AnalyticsMethods.Prototypes.Models.Item;
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
 * Created by Arham Muslim
 * on 21-Feb-17.
 */
public class ColumnBased extends AnalyticsMethod {

    public ColumnBased() {
        this.setInput(new OpenLAPDataSet());
        this.setOutput(new OpenLAPDataSet());
        this.setParams(new OpenLAPDynamicParams());

        try {
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_name", OpenLAPColumnDataType.Text, true, "Items to count", "List of items to count")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("group_column", OpenLAPColumnDataType.Text, true, "Group by column", "List of of items which will be used to group the items to count.")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("users", OpenLAPColumnDataType.Text, false, "User", "List of users for whome items should be counted if [Unique items] count types is selected.")
            );

            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("group_column", OpenLAPColumnDataType.Text, true, "Group by column", "List of of items which will be used to group the items to count.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_name", OpenLAPColumnDataType.Text, true, "Item Names", "List of items in the list")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_count", OpenLAPColumnDataType.Numeric, true, "Item Count", "Number of time each item occurred in the list for each item in the group by column")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("search_type", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Counting type", "Count all item occurrences per user or only single item per user. For [Unique items] count type, the [User] column is required", "All items", "All items,Unique items", true)
            );
        } catch (OpenLAPDynamicParamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Count items based on specified column";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Count the occurrences of items for each unique value in the specified column. E.g. if the 'group by column' contains unique values G1,G2 and items list contains I1,I2,I3 than it will calculate how many time I1 occurred with G1 and G2. Similarly for I2 and so on.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    @Override
    protected void implementationExecution() {
        String search_type = (String)this.getParams().getParams().get("search_type").getValue();
        if(search_type.equals("All items")) {
            Map<Item, Integer> groupItemCount = new TreeMap<Item, Integer>(
                    new Comparator<Item>() {
                        public int compare(Item p1, Item p2) {
                            if (p1.getColumn().equals(p2.getColumn()))
                                return p1.getTitle().compareTo(p2.getTitle());
                            else {
                                return p1.getColumn().compareTo(p2.getColumn());
                            }
                        }
                    }
            );

            Item item = null;

            Iterator titles = this.getInput().getColumns().get("item_name").getData().iterator();
            Iterator groups = this.getInput().getColumns().get("group_column").getData().iterator();

            while (titles.hasNext() && groups.hasNext()) {

                String title = (String) titles.next();
                String group = (String) groups.next();

                item = new Item(title, group);

                if (groupItemCount.containsKey(item)) {
                    Integer count = groupItemCount.get(item);
                    groupItemCount.put(item, count + 1);
                } else {
                    groupItemCount.put(item, 1);
                }
            }

            Set<Map.Entry<Item, Integer>> itemMonthlyCountSet = groupItemCount.entrySet();

            for (Map.Entry<Item, Integer> entry : itemMonthlyCountSet) {
                getOutput().getColumns().get("item_name").getData().add(entry.getKey().getTitle());
                getOutput().getColumns().get("group_column").getData().add(entry.getKey().getColumn());
                getOutput().getColumns().get("item_count").getData().add(entry.getValue());
            }
        }
        else{
            Map<Item, List<String>> groupItemCount = new TreeMap<Item, List<String>>(
                    new Comparator<Item>() {
                        public int compare(Item p1, Item p2) {
                            if (p1.getColumn().equals(p2.getColumn()))
                                return p1.getTitle().compareTo(p2.getTitle());
                            else {
                                return p1.getColumn().compareTo(p2.getColumn());
                            }
                        }
                    }
            );

            Item item = null;

            Iterator titles = this.getInput().getColumns().get("item_name").getData().iterator();
            Iterator groups = this.getInput().getColumns().get("group_column").getData().iterator();
            Iterator users = this.getInput().getColumns().get("users").getData().iterator();

            while (titles.hasNext() && groups.hasNext() && users.hasNext()) {

                String title = (String) titles.next();
                String group = (String) groups.next();
                String user = (String) users.next();

                item = new Item(title, group);

                if (groupItemCount.containsKey(item)) {
                    List<String> usersList = groupItemCount.get(item);

                    if(!usersList.contains(user))
                        usersList.add(user);
                } else {
                    List<String> newUsersList = new ArrayList<String>();
                    newUsersList.add(user);
                    groupItemCount.put(item, newUsersList);
                }
            }

            Set<Map.Entry<Item, List<String>>> itemMonthlyCountSet = groupItemCount.entrySet();

            for (Map.Entry<Item, List<String>> entry : itemMonthlyCountSet) {
                getOutput().getColumns().get("item_name").getData().add(entry.getKey().getTitle());
                getOutput().getColumns().get("group_column").getData().add(entry.getKey().getColumn());
                getOutput().getColumns().get("item_count").getData().add(entry.getValue().size());
            }
        }
    }

    @Override
    public Boolean hasPMML() {
        return null;
    }

    @Override
    public InputStream getPMMLInputStream() {
        return null;
    }
}
