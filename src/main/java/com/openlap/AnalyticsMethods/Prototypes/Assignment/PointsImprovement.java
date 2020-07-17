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
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Arham Muslim
 * on 29-Nov-16.
 */
public class PointsImprovement extends AnalyticsMethod {
    public PointsImprovement() {
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
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("total_marks", OpenLAPColumnDataType.Numeric, true, "Total Marks", "Total marks of the assignment.")
            );

            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("assign_title", OpenLAPColumnDataType.Text, true, "Assignment Title", "List of assignment titles.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("highest_value", OpenLAPColumnDataType.Numeric, true, "Highest Point", "Highest point achieved in the assignment.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("upper_quartile", OpenLAPColumnDataType.Numeric, true, "Upper Quartile ", "Upper quartile of the achieved points in the assignment.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("median", OpenLAPColumnDataType.Numeric, true, "Median", "Median of the points in the assignment.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("lower_quartile", OpenLAPColumnDataType.Numeric, true, "Lower Quartile", "Lower quartile of the achieved points in the assignment.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("lowest_value", OpenLAPColumnDataType.Numeric, true, "Lowest Point", "Lowest point achieved in the assignment.")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("improvement_type", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Improvement Type", "How should the improvement be calcuated. Different between the points of first and the last attempt or the different between the minimum and maximum points obtained in all attempts.", "First and Last Attempts", "First and Last Attempts,Min and Max Points Obtained", true)
            );
        } catch (OpenLAPDynamicParamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Calculate median student progress";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Calculate the progress students have made in Moodle quizzes based on their first attempt and the last attempt or the lowest and highest points obtained.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    protected void implementationExecution() {
        String improvementType = (String)this.getParams().getParams().get("improvement_type").getValue();
        boolean isFirstLast = improvementType.equals("First and Last Attempts");

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        HashMap<String, Double> assignmentMaxMarks = new HashMap<>();
        HashMap<String, Integer> assignmentTitleTimes = new HashMap<String, Integer>();
        Map<String, Map<String, List<Marks>>> assignmentPoints = new TreeMap<String, Map<String, List<Marks>>>();

        Iterator titles = this.getInput().getColumns().get("assign_title").getData().iterator();
        Iterator timestamps = this.getInput().getColumns().get("timestamp").getData().iterator();
        Iterator users = this.getInput().getColumns().get("user").getData().iterator();
        Iterator marks = this.getInput().getColumns().get("marks").getData().iterator();
        Iterator totalMarks = this.getInput().getColumns().get("total_marks").getData().iterator();

        while (titles.hasNext() && timestamps.hasNext() && users.hasNext()&& marks.hasNext()) {
            String title = (String) titles.next();
            Integer timestamp = (Integer) timestamps.next();
            String user = (String) users.next();
            Double mark = (Double) marks.next();
            Double totalMark = (Double) totalMarks.next();

            if(!assignmentMaxMarks.containsKey(title))
                assignmentMaxMarks.put(title, totalMark);

            if (assignmentPoints.containsKey(title)) {

                if(assignmentTitleTimes.get(title)>timestamp)
                    assignmentTitleTimes.put(title, timestamp);

                Map<String, List<Marks>> studentPoints = assignmentPoints.get(title);

                if(studentPoints.containsKey(user)){
                    List<Marks> item = studentPoints.get(user);

                    item.add(new Marks(timestamp, mark));
                }
                else {
                    List<Marks> newPointsList = new ArrayList<Marks>();
                    newPointsList.add(new Marks(timestamp, mark));
                    studentPoints.put(user, newPointsList);
                }
            } else {
                assignmentTitleTimes.put(title, timestamp);

                List<Marks> newPointsList = new ArrayList<Marks>();
                newPointsList.add(new Marks(timestamp, mark));
                Map<String, List<Marks>> userPoints = new HashMap<String, List<Marks>>();
                userPoints.put(user, newPointsList);
                assignmentPoints.put(title, userPoints);
            }
        }

        Comparator<String> comparator = new ValueComparator<>(assignmentTitleTimes);
        TreeMap<String, Integer> sortedTitleTimes = new TreeMap<>(comparator);
        sortedTitleTimes.putAll(assignmentTitleTimes);

        Set<Map.Entry<String, Integer>> sortedTitlesSet = sortedTitleTimes.entrySet();

        for (Map.Entry<String, Integer> sortedTitle : sortedTitlesSet) {

            Double medianValue = 0.0;
            Double upperValue = 0.0;
            Double lowerValue = 0.0;
            Double minValue = 0.0;
            Double maxValue = 0.0;

            if(assignmentMaxMarks.get(sortedTitle.getKey()) != 0) {
                Set<Map.Entry<String, List<Marks>>> allUsersPoints = assignmentPoints.get(sortedTitle.getKey()).entrySet();

                ArrayList<Double> pointsList = new ArrayList<>();
                for (Map.Entry<String, List<Marks>> userPoints : allUsersPoints) {

                    Marks marks1 = null, marks2 = null;

                    for (Marks mark : userPoints.getValue()) {
                        if (marks1 == null || marks2 == null) {
                            marks1 = mark;
                            marks2 = mark;
                        } else {
                            if (isFirstLast) {
                                if (mark.getTimestamp() < marks1.getTimestamp())
                                    marks1 = mark;
                                else if (mark.getTimestamp() > marks2.getTimestamp())
                                    marks2 = mark;
                            } else {
                                if (mark.getMark() < marks1.getMark())
                                    marks1 = mark;
                                else if (mark.getMark() > marks2.getMark())
                                    marks2 = mark;
                            }
                        }
                    }
                    Double userProgress = getStudentProgress(marks1.getMark(), marks2.getMark(), assignmentMaxMarks.get(sortedTitle.getKey()));
                    //System.out.println("Student:" + userPoints.getKey() + " - progress:"+userProgress + " - marks1:" + marks1.getMark() + " - marks2:" + marks2.getMark());
                    pointsList.add(userProgress);
                }

                pointsList.sort((Double p1, Double p2) -> p1.compareTo(p2));

                //System.out.println(sortedTitle.getKey() + " : " + pointsList);

                minValue = pointsList.get(0);
                maxValue = pointsList.get(pointsList.size() - 1);

                int listSize = pointsList.size();
                if ((listSize % 2) == 0) {
                    medianValue = (pointsList.get(listSize / 2 - 1) + pointsList.get(listSize / 2)) / 2;

                    if (listSize > 2) {
                        int newListSize = listSize / 2;
                        if ((newListSize % 2) == 0) {
                            int firstValue = newListSize / 2 - 1;
                            int secondValue = newListSize / 2;

                            lowerValue = (pointsList.get(firstValue) + pointsList.get(secondValue)) / 2;
                            upperValue = (pointsList.get(firstValue + newListSize) + pointsList.get(secondValue + newListSize)) / 2;
                        } else {
                            int meanIndex = newListSize / 2;
                            lowerValue = pointsList.get(meanIndex);
                            upperValue = pointsList.get(meanIndex + newListSize);
                        }
                    } else {
                        lowerValue = pointsList.get(0);
                        upperValue = pointsList.get(1);
                    }
                } else {
                    if (listSize > 2) {
                        int meanIndex = listSize / 2;
                        medianValue = pointsList.get(meanIndex);

                        int newListSize = meanIndex;
                        if ((newListSize % 2) == 0) {
                            int firstValue = newListSize / 2 - 1;
                            int secondValue = newListSize / 2;

                            lowerValue = (pointsList.get(firstValue) + pointsList.get(secondValue)) / 2;
                            upperValue = (pointsList.get(firstValue + newListSize + 1) + pointsList.get(secondValue + newListSize + 1)) / 2;
                        } else {
                            int newIndex = newListSize / 2;
                            lowerValue = pointsList.get(newIndex);
                            upperValue = pointsList.get(newIndex + newListSize + 1);
                        }
                    } else {
                        medianValue = pointsList.get(0);
                        lowerValue = pointsList.get(0);
                        upperValue = pointsList.get(0);
                    }
                }
            }

            getOutput().getColumns().get("assign_title").getData().add(sortedTitle.getKey());
            getOutput().getColumns().get("highest_value").getData().add(decimalFormat.format(maxValue));
            getOutput().getColumns().get("upper_quartile").getData().add(decimalFormat.format(upperValue));
            getOutput().getColumns().get("median").getData().add(decimalFormat.format(medianValue));
            getOutput().getColumns().get("lower_quartile").getData().add(decimalFormat.format(lowerValue));
            getOutput().getColumns().get("lowest_value").getData().add(decimalFormat.format(minValue));

        }
    }

    public Boolean hasPMML() {
        return null;
    }

    public InputStream getPMMLInputStream() {
        return null;
    }

    private Double getStudentProgress(Double f1, Double f2, Double max){
        return ((f2-f1)/max)*100;
    }
}