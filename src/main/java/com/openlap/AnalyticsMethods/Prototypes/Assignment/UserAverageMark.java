package com.openlap.AnalyticsMethods.Prototypes.Assignment;

import com.openlap.AnalyticsMethods.Prototypes.Models.Marks;
import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dynamicparam.OpenLAPDynamicParams;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.template.AnalyticsMethod;

import java.io.InputStream;
import java.util.*;

public class UserAverageMark extends AnalyticsMethod {
    public UserAverageMark() {
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
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("user", OpenLAPColumnDataType.Text, true, "Submitted By", "List of students who submitted this solution.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("average_marks", OpenLAPColumnDataType.Numeric, true, "Average Marks", "Average marks of students in assignments.")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Calculate average assignment marks per user";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Calculate the average marks of each student based on the total number of assignment.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    protected void implementationExecution() {
        try {
            Map<String, Map<String, Marks>> finalMarks = new TreeMap<String, Map<String, Marks>>();

            Iterator titles = this.getInput().getColumns().get("assign_title").getData().iterator();
            Iterator timestamps = this.getInput().getColumns().get("timestamp").getData().iterator();
            Iterator users = this.getInput().getColumns().get("user").getData().iterator();
            Iterator marks = this.getInput().getColumns().get("marks").getData().iterator();

            ArrayList<String> assignmentTitles = new ArrayList<String>();

            //System.out.println("0");

            while (titles.hasNext() && timestamps.hasNext() && users.hasNext() && marks.hasNext()) {
                String title = (String) titles.next();
                Integer timestamp = (Integer) timestamps.next();
                String userss = (String) users.next();
                Double mark = (Double) marks.next();

                //System.out.println(title + " - " + timestamp + " - " + userss + " - " + mark);

                if( userss != null && !userss.isEmpty()){

                    if (!assignmentTitles.contains(title)) //calculating the total number of
                        assignmentTitles.add(title);

                    String[] userArray = userss.split(",");

                    for(String user: userArray) {
                        if (finalMarks.containsKey(user)) {
                            Map<String, Marks> userMarks = finalMarks.get(user);

                            if (userMarks.containsKey(title)) {
                                Marks item = userMarks.get(title);

                                if (item.getTimestamp() < timestamp)
                                    userMarks.put(title, new Marks(timestamp, mark));
                            } else {
                                userMarks.put(title, new Marks(timestamp, mark));
                            }
                        } else {

                            Map<String, Marks> userMarks = new HashMap<String, Marks>();
                            userMarks.put(title, new Marks(timestamp, mark));
                            finalMarks.put(user, userMarks);
                        }
                    }
                }
            }
            //System.out.println("1");

            Set<Map.Entry<String, Map<String, Marks>>> assignments = finalMarks.entrySet();

            for (Map.Entry<String, Map<String, Marks>> entry : assignments) {

                Set<Map.Entry<String, Marks>> userMarks = entry.getValue().entrySet();

                float marksSum = 0;
                for (Map.Entry<String, Marks> userMark : userMarks) {
                    marksSum += userMark.getValue().getMark();
                }

                double average = Math.round((marksSum / assignmentTitles.size()) * 100.0) / 100.0;

                //System.out.println(entry.getKey() + " - " + average);

                getOutput().getColumns().get("user").getData().add(entry.getKey());
                getOutput().getColumns().get("average_marks").getData().add(average);
            }

            //System.out.println("2");
        }
        catch (Exception exc){
            System.out.println(exc.toString());
        }
    }

    public Boolean hasPMML() {
        return null;
    }

    public InputStream getPMMLInputStream() {
        return null;
    }
}

