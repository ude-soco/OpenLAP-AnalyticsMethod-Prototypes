package com.openlap.AnalyticsMethods.Prototypes.Assignment;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Arham Muslim
 * on 29-Nov-16.
 */
public class AverageTime extends AnalyticsMethod {
    public AverageTime() {
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
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("time_name", OpenLAPColumnDataType.Text, true, "Time Text", "List of time string in which the associated item occurred e.g. Day:20-OCT-2017, Week:43 (Oct-2017), Month:Oct-2017, Year:2017")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("overview", OpenLAPColumnDataType.Numeric, true, "Average Marks", "Average marks of students in assignments.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("median", OpenLAPColumnDataType.Numeric, true, "Median Marks", "Median of the marks in the assignment.")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("multiple_submission", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Allow Multiple Submission?", "Allow multple submission from the same students to be considered as separate submissions or allow only single submission per student and the last grade will be used.", "Single Submission", "Single Submission,Multiple Submission", true)
            );
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("time_period", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Time period", "Specify the time span for which you would like to calculate overview.", "Week", "Day,Week,Month,Year", true)
            );
        } catch (OpenLAPDynamicParamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Calculate average and median assignment marks based on time";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Calculate the average and median of the assignment points for the selected time intervals. E.g. selecting Week will give you the average and median of the assignment corrected in each week.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    protected void implementationExecution() {
        String multiSubmission = (String)this.getParams().getParams().get("multiple_submission").getValue();
        boolean allowMultipleSubmission = !multiSubmission.equals("Single Submission");
        String timePeriod = (String)this.getParams().getParams().get("time_period").getValue();

        SimpleDateFormat dateSaveFormat;
        SimpleDateFormat dateExportFormat;
        switch (timePeriod){
            case "Day":
                dateSaveFormat = new SimpleDateFormat("dd-MMM-yyyy");
                dateExportFormat = new SimpleDateFormat("dd-MMM-yyyy");
                break;
            case "Month":
                dateSaveFormat = new SimpleDateFormat("MMM-yyyy");
                dateExportFormat = new SimpleDateFormat("MMM-yyyy");
                break;
            case "Year":
                dateSaveFormat = new SimpleDateFormat("yyyy");
                dateExportFormat = new SimpleDateFormat("yyyy");
                break;
            default:
                dateSaveFormat = new SimpleDateFormat("w-yyyy");
                dateExportFormat = new SimpleDateFormat("w (MMM-yyyy)");
                break;
        }

        HashMap<String, Integer> titleTimes = new HashMap<String, Integer>();
        //date - assignments - users - marks
        Map<Date,Map<String, Map<String, List<Marks>>>> averagePoints = new TreeMap<Date, Map<String, Map<String, List<Marks>>>>(
                new Comparator<Date>() {
                    public int compare(Date p1, Date p2) {
                        return p1.compareTo(p2);
                    }
                }
        );
        Iterator titles = this.getInput().getColumns().get("assign_title").getData().iterator();
        Iterator timestamps = this.getInput().getColumns().get("timestamp").getData().iterator();
        Iterator users = this.getInput().getColumns().get("user").getData().iterator();
        Iterator marks = this.getInput().getColumns().get("marks").getData().iterator();

        while (titles.hasNext() && timestamps.hasNext() && users.hasNext() && marks.hasNext()) {
            String title = (String) titles.next();
            Integer timestamp = (Integer) timestamps.next();
            String user = (String) users.next();
            Double mark = (Double) marks.next();

            if(title!=null && timestamp!=null && user!=null && mark!=null) {

                Date date = new Date(timestamp * 1000L);
                Date saveDate = null;
                try {
                    saveDate = dateSaveFormat.parse(dateSaveFormat.format(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(saveDate != null) {
                    if (averagePoints.containsKey(saveDate)) {
                        Map<String, Map<String, List<Marks>>> dateAssignments = averagePoints.get(saveDate);

                        if (dateAssignments.containsKey(title)) {
                            Map<String, List<Marks>> assignmentUsers = dateAssignments.get(title);

                            if (assignmentUsers.containsKey(user)) {
                                List<Marks> item = assignmentUsers.get(user);

                                if (allowMultipleSubmission)
                                    item.add(new Marks(timestamp, mark));
                                else {
                                    if (item.get(0).getTimestamp() < timestamp) {
                                        item.get(0).setMark(mark);
                                        item.get(0).setTimestamp(timestamp);
                                    }
                                }
                            } else {
                                List<Marks> newMarksList = new ArrayList<Marks>();
                                newMarksList.add(new Marks(timestamp, mark));
                                assignmentUsers.put(user, newMarksList);
                            }
                        } else {
                            List<Marks> newMarksList = new ArrayList<Marks>();
                            newMarksList.add(new Marks(timestamp, mark));

                            Map<String, List<Marks>> assignmentUsers = new HashMap<>();
                            assignmentUsers.put(user, newMarksList);

                            dateAssignments.put(title, assignmentUsers);
                        }
                    } else {
                        List<Marks> newMarksList = new ArrayList<Marks>();
                        newMarksList.add(new Marks(timestamp, mark));

                        Map<String, List<Marks>> assignmentUsers = new HashMap<>();
                        assignmentUsers.put(user, newMarksList);

                        Map<String, Map<String, List<Marks>>> dateAssignments = new HashMap<>();
                        dateAssignments.put(title, assignmentUsers);

                        averagePoints.put(saveDate, dateAssignments);
                    }
                }
            }
        }


        Set<Date> datesKeySet = averagePoints.keySet();

        for (Date dateKey : datesKeySet) {
            Map<String, Map<String, List<Marks>>> dateAssignments = averagePoints.get(dateKey);

            double marksSum = 0;
            int totalRecords = 0;
            double medianValue = 0;

            Set<Map.Entry<String, Map<String, List<Marks>>>> dateAssignmentsSet = dateAssignments.entrySet();

            ArrayList<Double> medianList = new ArrayList<>();

            for (Map.Entry<String, Map<String, List<Marks>>> dateAssignment : dateAssignmentsSet) {

                Set<Map.Entry<String, List<Marks>>> assignmentUsersSet = dateAssignment.getValue().entrySet();

                for (Map.Entry<String, List<Marks>> assignmentUsers : assignmentUsersSet) {
                    for(Marks mark : assignmentUsers.getValue()) {
                        marksSum += mark.getMark();
                        totalRecords++;
                        medianList.add(mark.getMark());
                    }
                }
            }

            double average = Math.round( (marksSum/totalRecords) * 100.0 ) / 100.0;

            medianList.sort((Double p1, Double p2) -> p1.compareTo(p2));
            int listSize = medianList.size();
            if((listSize % 2) == 0){
                medianValue = (medianList.get(listSize/2-1) + medianList.get(listSize/2))/2;
            }
            else {
                if(listSize>2) {
                    int meanIndex = listSize/2;
                    medianValue = medianList.get(meanIndex);
                }
                else{
                    medianValue = medianList.get(0);
                }
            }

            getOutput().getColumns().get("time_name").getData().add(dateExportFormat.format(dateKey));
            getOutput().getColumns().get("overview").getData().add(average);
            getOutput().getColumns().get("median").getData().add(medianValue);
        }
    }

    public Boolean hasPMML() {
        return null;
    }

    public InputStream getPMMLInputStream() {
        return null;
    }
}
