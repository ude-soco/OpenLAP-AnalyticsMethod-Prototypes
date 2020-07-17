package com.openlap.AnalyticsMethods.Prototypes.Assignment;

import com.openlap.AnalyticsMethods.Prototypes.Comparators.ValueComparator;
import com.openlap.AnalyticsMethods.Prototypes.Models.Marks;
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
 * on 29-Nov-16.
 */
public class AverageMarks extends AnalyticsMethod {
    public AverageMarks() {
        this.setInput(new OpenLAPDataSet());
        this.setOutput(new OpenLAPDataSet());
        this.setParams(new OpenLAPDynamicParams());

        try {
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("assign_title", OpenLAPColumnDataType.Text, true, "Assignment Title", "List of assignment titles")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("timestamp", OpenLAPColumnDataType.Numeric, true, "Timestamp", "List of timestamps to take the latest grade of user.")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("user", OpenLAPColumnDataType.Text, true, "Submitted By", "List of students who submitted this solution.")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("marks", OpenLAPColumnDataType.Numeric, true, "Obtained Marks", "Obtained marks in the assignment.")
            );

            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("assign_title", OpenLAPColumnDataType.Text, true, "Assignment Title", "List of assignment titles.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("average_marks", OpenLAPColumnDataType.Numeric, true, "Average Marks", "Average marks of students in assignments.")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("multiple_submission", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Allow Multiple Submission?", "Allow multiple submission from the same students to be considered as separate submissions or allow only single submission per student and the last grade will be used.", "Single Submission", "Single Submission,Multiple Submission", true)
            );
        } catch (OpenLAPDynamicParamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Calculate average assignment marks";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Calculate the average marks of students per assignment.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    protected void implementationExecution() {
        String multiSubmission = (String)this.getParams().getParams().get("multiple_submission").getValue();
        boolean allowMultipleSubmission = !multiSubmission.equals("Single Submission");

        HashMap<String, Integer> titleTimes = new HashMap<String, Integer>();
        Map<String, Map<String, List<Marks>>> finalMarks = new TreeMap<String, Map<String, List<Marks>>>();

        Iterator titles = this.getInput().getColumns().get("assign_title").getData().iterator();
        Iterator timestamps = this.getInput().getColumns().get("timestamp").getData().iterator();
        Iterator users = this.getInput().getColumns().get("user").getData().iterator();
        Iterator marks = this.getInput().getColumns().get("marks").getData().iterator();

        while (titles.hasNext() && timestamps.hasNext() && users.hasNext()&& marks.hasNext()) {
            String title = (String) titles.next();
            Integer timestamp = (Integer) timestamps.next();
            String user = (String) users.next();
            Double mark = (Double) marks.next();

            if (finalMarks.containsKey(title)) {

                if(titleTimes.get(title)>timestamp)
                    titleTimes.put(title, timestamp);

                Map<String, List<Marks>> userMarks = finalMarks.get(title);

                if(userMarks.containsKey(user)){
                    List<Marks> item = userMarks.get(user);

                    if(allowMultipleSubmission)
                        item.add(new Marks(timestamp, mark));
                    else{
                        if(item.get(0).getTimestamp()<timestamp){
                            item.get(0).setMark(mark);
                            item.get(0).setTimestamp(timestamp);
                        }
                    }
                }
                else {
                    List<Marks> newMarksList = new ArrayList<Marks>();
                    newMarksList.add(new Marks(timestamp, mark));
                    userMarks.put(user, newMarksList);
                }
            } else {
                titleTimes.put(title, timestamp);

                List<Marks> newMarksList = new ArrayList<Marks>();
                newMarksList.add(new Marks(timestamp, mark));
                Map<String, List<Marks>> userMarks = new HashMap<String, List<Marks>>();
                userMarks.put(user, newMarksList);
                finalMarks.put(title, userMarks);
            }
        }

        Comparator<String> comparator = new ValueComparator<String, Integer>(titleTimes);
        TreeMap<String, Integer> sortedTitleTimes = new TreeMap<String, Integer>(comparator);
        sortedTitleTimes.putAll(titleTimes);

        //Map<String, Integer> sortedTitleTimes = sortByValues(titleTimes);

        Set<Map.Entry<String, Integer>> titleSet = sortedTitleTimes.entrySet();

        for (Map.Entry<String, Integer> keyTitle : titleSet) {

            Set<Map.Entry<String, List<Marks>>> userMarks = finalMarks.get(keyTitle.getKey()).entrySet();

            float marksSum = 0;
            int totalRecords = 0;
            for (Map.Entry<String, List<Marks>> userMark : userMarks) {
                for(Marks mark : userMark.getValue()) {
                    marksSum += mark.getMark();
                    totalRecords++;
                }
            }

            double average = Math.round( (marksSum/totalRecords) * 100.0 ) / 100.0;

            //System.out.println(keyTitle.getValue() + " - " + keyTitle.getKey() + " - " + average);

            getOutput().getColumns().get("assign_title").getData().add(keyTitle.getKey());
            getOutput().getColumns().get("average_marks").getData().add(average);
        }
    }

    public Boolean hasPMML() {
        return null;
    }

    public InputStream getPMMLInputStream() {
        return null;
    }
}