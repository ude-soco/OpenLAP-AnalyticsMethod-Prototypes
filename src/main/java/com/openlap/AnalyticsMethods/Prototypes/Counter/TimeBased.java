package com.openlap.AnalyticsMethods.Prototypes.Counter;

import com.openlap.AnalyticsMethods.Prototypes.Models.TimedItem;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Arham Muslim
 * on 22-Mar-16.
 */
public class TimeBased extends AnalyticsMethod {

    public TimeBased() {
        this.setInput(new OpenLAPDataSet());
        this.setOutput(new OpenLAPDataSet());
        this.setParams(new OpenLAPDynamicParams());

        try {
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_name", OpenLAPColumnDataType.Text, true, "Items to count", "List of items to count")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("timestamp", OpenLAPColumnDataType.Numeric, true, "Timestamp", "List of timestamps related to the items list")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("users", OpenLAPColumnDataType.Text, false, "User", "List of users for whose items should be counted if [Unique items] count types is selected.")
            );

            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("time_name", OpenLAPColumnDataType.Text, true, "Time Text", "List of time string in which the associated item occurred e.g. Day:20-OCT-2017, Week:43 (Oct-2017), Month:Oct-2017, Year:2017")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_name", OpenLAPColumnDataType.Text, true, "Item Names", "List of items in the list")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_count", OpenLAPColumnDataType.Numeric, true, "Item Count", "Number of time each item occurred in the list")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("time_period", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Time period", "Specify the time span for which you would like to calculate overview.", "Week", "Day,Week,Month,Year", true)
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
        return "Count items per day/week/month/year";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Count the occurrences of items for each day/week/month/year.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    @Override
    protected void implementationExecution() {
        //LinkedHashMap<Item, Integer> itemMonthlyCount = new LinkedHashMap<Item, Integer>();
        String search_type = (String)this.getParams().getParams().get("search_type").getValue();
        String timePeriod = (String)this.getParams().getParams().get("time_period").getValue();

        SimpleDateFormat saveDateFormat;
        SimpleDateFormat exportDateFormat;
        switch (timePeriod){
            case "Day":
                saveDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                exportDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                break;
            case "Month":
                saveDateFormat = new SimpleDateFormat("MMM-yyyy");
                exportDateFormat = new SimpleDateFormat("MMM-yyyy");
                break;
            case "Year":
                saveDateFormat = new SimpleDateFormat("yyyy");
                exportDateFormat = new SimpleDateFormat("yyyy");
                break;
            default:
                saveDateFormat = new SimpleDateFormat("w-yyyy");
                exportDateFormat = new SimpleDateFormat("w (MMM-yyyy)");
                break;
        }

        if(search_type.equals("All items")) {
            Map<TimedItem, Integer> itemMonthlyCount = new TreeMap<TimedItem, Integer>(
                    new Comparator<TimedItem>() {
                        public int compare(TimedItem p1, TimedItem p2) {
                        /*if(p1.getTitle().equals(p2.getTitle()))
                            return p1.getDate().compareTo(p2.getDate());
                        else{
                            return p1.getTitle().compareTo(p2.getTitle());
                        }*/
                            if (p1.getDate().equals(p2.getDate()))
                                return p1.getTitle().compareTo(p2.getTitle());
                            else {
                                return p1.getDate().compareTo(p2.getDate());
                            }
                        }
                    }
            );

            TimedItem item = null;

            Iterator titles = this.getInput().getColumns().get("item_name").getData().iterator();
            Iterator timestamps = this.getInput().getColumns().get("timestamp").getData().iterator();

            while (titles.hasNext() && timestamps.hasNext()) {

                String title = (String) titles.next();
                Integer timestamp = (Integer) timestamps.next();

                Date date = new Date(timestamp * 1000L);

                try {
                    item = new TimedItem(title, saveDateFormat.parse(saveDateFormat.format(date)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if (itemMonthlyCount.containsKey(item)) {
                    Integer count = itemMonthlyCount.get(item);
                    itemMonthlyCount.put(item, count + 1);
                } else {
                    itemMonthlyCount.put(item, 1);
                }
            }

            Set<Map.Entry<TimedItem, Integer>> itemMonthlyCountSet = itemMonthlyCount.entrySet();

            for (Map.Entry<TimedItem, Integer> entry : itemMonthlyCountSet) {
                getOutput().getColumns().get("item_name").getData().add(entry.getKey().getTitle());
                getOutput().getColumns().get("time_name").getData().add("Week-" + exportDateFormat.format(entry.getKey().getDate()));
                getOutput().getColumns().get("item_count").getData().add(entry.getValue());
            }
        }
        else{
            Map<TimedItem, List<String>> itemMonthlyCount = new TreeMap<TimedItem, List<String>>(
                    new Comparator<TimedItem>() {
                        public int compare(TimedItem p1, TimedItem p2) {
                            if (p1.getDate().equals(p2.getDate()))
                                return p1.getTitle().compareTo(p2.getTitle());
                            else {
                                return p1.getDate().compareTo(p2.getDate());
                            }
                        }
                    }
            );

            TimedItem item = null;

            Iterator titles = this.getInput().getColumns().get("item_name").getData().iterator();
            Iterator timestamps = this.getInput().getColumns().get("timestamp").getData().iterator();
            Iterator users = this.getInput().getColumns().get("users").getData().iterator();

            while (titles.hasNext() && timestamps.hasNext() && users.hasNext()) {

                String title = (String) titles.next();
                Integer timestamp = (Integer) timestamps.next();
                String user = (String) users.next();

                Date date = new Date(timestamp * 1000L);

                try {
                    item = new TimedItem(title, saveDateFormat.parse(saveDateFormat.format(date)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (itemMonthlyCount.containsKey(item)) {
                    List<String> usersList = itemMonthlyCount.get(item);

                    if(!usersList.contains(user))
                        usersList.add(user);
                } else {
                    List<String> newUsersList = new ArrayList<String>();
                    newUsersList.add(user);
                    itemMonthlyCount.put(item, newUsersList);
                }
            }

            Set<Map.Entry<TimedItem, List<String>>> itemMonthlyCountSet = itemMonthlyCount.entrySet();

            for (Map.Entry<TimedItem, List<String>> entry : itemMonthlyCountSet) {
                getOutput().getColumns().get("item_name").getData().add(entry.getKey().getTitle());
                getOutput().getColumns().get("time_name").getData().add("Week-" + exportDateFormat.format(entry.getKey().getDate()));
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

