package com.openlap.AnalyticsMethods.Prototypes.Clustering;

import com.openlap.AnalyticsMethods.Prototypes.Models.ClusterData;
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
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kmean2NumericSummary extends AnalyticsMethod {
    public Kmean2NumericSummary() {
        this.setInput(new OpenLAPDataSet());
        this.setOutput(new OpenLAPDataSet());
        this.setParams(new OpenLAPDynamicParams());

        try {
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_1", OpenLAPColumnDataType.Numeric, true, "Numeric Attribute 1", "Numeric attribute 1")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_2", OpenLAPColumnDataType.Numeric, true, "Numeric Attribute 2", "Numeric attribute 2")
            );


            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("cluster_name", OpenLAPColumnDataType.Text, true, "Cluster ID (E.g. Cluster 1, Cluster 2)", "The cluster id for each item in the format Cluster 1, Cluster 2")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_count", OpenLAPColumnDataType.Numeric, true, "Items in cluster", "Number of items in cluster")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_1_centroid", OpenLAPColumnDataType.Numeric, true, "Attribute 1 centroid", "Centroid value for the attribute 1")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_1_max", OpenLAPColumnDataType.Numeric, true, "Attribute 1 maximum value", "Maximum value for the attribute 1")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_1_min", OpenLAPColumnDataType.Numeric, true, "Attribute 1 minimum value", "Minimum value for the attribute 1")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_2_centroid", OpenLAPColumnDataType.Numeric, true, "Attribute 2 centroid", "Centroid value for the attribute 2")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_2_max", OpenLAPColumnDataType.Numeric, true, "Attribute 2 maximum value", "Maximum value for the attribute 2")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_2_min", OpenLAPColumnDataType.Numeric, true, "Attribute 2 minimum value", "Minimum value for the attribute 2")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("cluster_count", OpenLAPDynamicParamType.Textbox, OpenLAPDynamicParamDataType.INTEGER, "Number of clusterers", "Specify the number of clusters to generate.", 5, "", true)
            );
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("kmean_seed", OpenLAPDynamicParamType.Textbox, OpenLAPDynamicParamDataType.INTEGER, "K-Mean seed value", "Specify the seed value for the K-Mean algorithm.", 10, "", true)
            );
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("kmean_iterations", OpenLAPDynamicParamType.Textbox, OpenLAPDynamicParamDataType.INTEGER, "K-Mean max iterations", "Specify the maximum iterations for the K-Mean algorithm.", 10, "", true)
            );
        } catch (OpenLAPDynamicParamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Get clusters summary based on two numeric attributes using K-Means";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Generate cluster using two numeric attributes using the K-Means algorithm and return information related to each cluster, including the name of the cluster and the number of items in it as well as the centroid, max, and min values of both attributes.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    @Override
    protected void implementationExecution() {
        try {
            ArrayList<Double> attribute_1 = this.getInput().getColumns().get("attribute_1").getData();
            ArrayList<Double> attribute_2 = this.getInput().getColumns().get("attribute_2").getData();

            int cluster_count = (Integer)this.getParams().getParams().get("cluster_count").getValue();
            int kmean_seed = (Integer)this.getParams().getParams().get("kmean_seed").getValue();
            int kmean_iterations = (Integer)this.getParams().getParams().get("kmean_iterations").getValue();

            HashMap<String, ArrayList> activity = new HashMap<String, ArrayList>();
            activity.put("dou-v0", attribute_1);
            activity.put("dou-v1", attribute_2);

            Instances activityDataset = createInstance(activity, "activitiesDataset");

            //initialize clusters and filtered clusters
            SimpleKMeans kmeansActivity = new SimpleKMeans();
            FilteredClusterer fcActivity = new FilteredClusterer();
            fcActivity.setClusterer(kmeansActivity);

            //set up kmeans clustering
            kmeansActivity.setSeed(kmean_seed);
            kmeansActivity.setPreserveInstancesOrder(true);
            kmeansActivity.setNumClusters(cluster_count);
            kmeansActivity.setMaxIterations(kmean_iterations);

            //cluster datasets
            fcActivity.buildClusterer(activityDataset);

            HashMap<String, ClusterData> clusters = new HashMap<String, ClusterData>();


            for (int i = 0; i < activityDataset.numInstances(); i++) {
                Instance inst = activityDataset.instance(i);
                int clusterId = kmeansActivity.clusterInstance(inst);
                clusterId += 1; //adding 1 to make it start from 1 instead of 0
                String clusterName = "Cluster "+ clusterId;

                double att1Value = inst.value(activityDataset.attribute("dou-v0"));
                int att1Int = (int)att1Value;

                double att2Dou = inst.value(activityDataset.attribute("dou-v1"));

                if(clusters.containsKey(clusterName)){
                    if(clusters.get(clusterName).getAtt1MaxValue() < att1Int)
                        clusters.get(clusterName).setAtt1MaxValue(att1Int);
                    else if(clusters.get(clusterName).getAtt1MinValue() > att1Int)
                        clusters.get(clusterName).setAtt1MinValue(att1Int);

                    if(clusters.get(clusterName).getAtt2MaxValue() < att2Dou)
                        clusters.get(clusterName).setAtt2MaxValue(att2Dou);
                    else if(clusters.get(clusterName).getAtt2MinValue() > att2Dou)
                        clusters.get(clusterName).setAtt2MinValue(att2Dou);

                    clusters.get(clusterName).setItemCount(clusters.get(clusterName).getItemCount()+1);
                }
                else{
                    clusters.put(clusterName, new ClusterData(clusterName, 1, att1Int, att1Int, att2Dou, att2Dou));
                }
            }


            Instances instances = kmeansActivity.getClusterCentroids();

            for (int i = 0; i < instances.size(); i++) {
                int clusterId = kmeansActivity.clusterInstance(instances.get(i));
                clusterId += 1; //adding 1 to make it start from 1 instead of 0
                String clusterName = "Cluster "+ clusterId;

                if(clusters.containsKey(clusterName)){
                    double att1Value = instances.get(i).value(activityDataset.attribute("dou-v0"));
                    clusters.get(clusterName).setAtt1CentroidValue(att1Value);

                    double att2Value = instances.get(i).value(activityDataset.attribute("dou-v1"));
                    clusters.get(clusterName).setAtt2CentroidValue(att2Value);
                }
            }

            for(Map.Entry<String, ClusterData> item : clusters.entrySet()){
                getOutput().getColumns().get("cluster_name").getData().add(item.getValue().getName());
                getOutput().getColumns().get("item_count").getData().add(item.getValue().getItemCount());
                getOutput().getColumns().get("attribute_1_centroid").getData().add(item.getValue().getAtt1CentroidValue());
                getOutput().getColumns().get("attribute_1_max").getData().add(item.getValue().getAtt1MaxValue());
                getOutput().getColumns().get("attribute_1_min").getData().add(item.getValue().getAtt1MinValue());
                getOutput().getColumns().get("attribute_2_centroid").getData().add(item.getValue().getAtt2CentroidValue());
                getOutput().getColumns().get("attribute_2_max").getData().add(item.getValue().getAtt2MaxValue());
                getOutput().getColumns().get("attribute_2_min").getData().add(item.getValue().getAtt2MinValue());
            }


            //get (userID -> activity cluster) data
//            HashMap<Double, ClusterData> clusterData = new HashMap<Double, ClusterData>();
//            for (int i = 0; i < activityDataset.numInstances(); i++) {
//                int x = fcActivity.clusterInstance(activityDataset.instance(i));
//                clusterData.put(activityDataset.get(i).value(0), new ClusterData(x));
//            }
//            System.out.println(clusterData);
//
//            //add (userID -> grade cluster) data to former map
//            for (int i = 0; i < gradesDataset.numInstances(); i++) {
//                int x = fcGrades.clusterInstance(gradesDataset.instance(i));
//                clusterData.get(gradesDataset.get(i).value(0)).setGradeCluster(x);
//            }
//
//            System.out.println(clusterData);

            //get relative order/ranking of clusters - for activity clusters
//            Double[] additiveValues = {0.0,0.0,0.0};
//            Instances centroidActivity = kmeansActivity.getClusterCentroids();
//            for (int i = 0; i < centroidActivity.numInstances(); i++) {
//                for (int j = 1; j < centroidActivity.numAttributes(); j++) {
//                    additiveValues[i] += centroidActivity.get(i).value(j);
//                }
//            }
//
//            HashMap<Integer,String> orderActivity = orderInMap(additiveValues[0], additiveValues[1], additiveValues[2]);

//            //reset array
//            additiveValues[0] = 0.0;
//            additiveValues[1] = 0.0;
//            additiveValues[2] = 0.0;
//
//            //relative order - for grades clusters
//            Instances centroidGrades = kmeansGrades.getClusterCentroids();
//            for (int i = 0; i < centroidGrades.numInstances(); i++) {
//                for (int j = 1; j < centroidGrades.numAttributes(); j++) {
//                    additiveValues[i] += centroidGrades.get(i).value(j);
//                }
//            }
//
//            HashMap<Integer,String> orderGrades = orderInMap(additiveValues[0], additiveValues[1], additiveValues[2]);
//

            //determine ratio of clusters
//            HashMap<String,Integer> clusterRatio = new HashMap<String,Integer>();
//            initClusterRatio(clusterRatio);
//
//            //get cluster counters and cluster-to-cluster-ratio
//            for (Map.Entry<Double,ClusterData> e : clusterData.entrySet()) {
//                //update entry for individual groups and the sum
//                clusterRatio.put("cAn", clusterRatio.get("cAn") + 1);
//                clusterRatio.put("cGn", clusterRatio.get("cGn") + 1);
//                //update specific cluster-to-cluster ratio entry
//                clusterRatio.put("cA"+e.getValue().getActivityCluster(), clusterRatio.get("cA"+e.getValue().getActivityCluster())+1);
//                clusterRatio.put("cG"+e.getValue().getGradeCluster(), clusterRatio.get("cG"+e.getValue().getGradeCluster())+1);
//
//                clusterRatio.put("cG"+e.getValue().getGradeCluster()+"A"+e.getValue().getActivityCluster(), clusterRatio.get("cG"+e.getValue().getGradeCluster()+"A"+e.getValue().getActivityCluster())+1);
//            }


            //OUTPUT TO OLAP DATASET

//            //.. for Ia-b (Ratio of grades to activity cluster)
//            for (Map.Entry<String,Integer> e : clusterRatio.entrySet()) {
//                //only get ratio entries
//                if (e.getKey().length() < 4)
//                    continue;
//
//                //get rank of value pair and create string
//                int gradeGroup = Integer.parseInt(e.getKey().substring(2, 3));
//                int activityGroup = Integer.parseInt(e.getKey().substring(4, 5));
//                String outputGroup = "Grades-" + orderGrades.get(gradeGroup) + "/Activity-" + orderActivity.get(activityGroup);
//
//                //output to OLAP dataset
//                getOutput().getColumns().get("cluster_combination_string").getData().add(outputGroup);
//                getOutput().getColumns().get("cluster_combination_ratio").getData().add((e.getValue()/(double)clusterRatio.get(e.getKey().substring(0, 3))));
//            }
//
//            //.. for IIa-IIe (Single users with respective activity/grades cluster membership
//            for (Map.Entry e : clusterData.entrySet()) {
//                getOutput().getColumns().get("user_id").getData().add(""+e.getKey());
//                getOutput().getColumns().get("user_cluster_activity").getData().add(clusterData.get(e.getKey()).getActivityCluster());
//                getOutput().getColumns().get("user_cluster_activity_rank").getData().add(orderActivity.get(clusterData.get(e.getKey()).getActivityCluster()));
//                getOutput().getColumns().get("user_cluster_grades").getData().add(clusterData.get(e.getKey()).getGradeCluster());;
//                getOutput().getColumns().get("user_cluster_grades_rank").getData().add(orderGrades.get(clusterData.get(e.getKey()).getGradeCluster()));
//            }

        }
        catch (Exception exc) {
            System.out.println(exc.getMessage());
        }
    }

    private Instances createInstance(HashMap<String,ArrayList> inputMap, String datasetTitle) {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        List<Instance> instances = new ArrayList<Instance>();

        boolean isFirstArray = true;
        int indexArray = 0;

        for (Map.Entry<String, ArrayList> inputArray : inputMap.entrySet()) {
            //initialize correct number of instances
            Attribute current = new Attribute(inputArray.getKey(), indexArray);
            if(isFirstArray) {
                for(int obj = 0; obj < inputArray.getValue().size(); obj++) {
                    instances.add(new DenseInstance(inputMap.size()));
                }
                isFirstArray = false;
            }

            String arrayType = inputArray.getKey().substring(0,3);

            //fill instances depending on input type
            for(int index = 0; index < inputArray.getValue().size(); index++) {
                if (arrayType.equals("dou")) {
                    String s = inputArray.getValue().get(index).toString();
                    double i = Double.parseDouble(s);
                    instances.get(index).setValue(current, i);
                } else if (arrayType.equals("int")) {
                    String s = inputArray.getValue().get(index).toString();
                    int i = Integer.parseInt(s);
                    instances.get(index).setValue(current, i);
                }
            }
            attributes.add(current);

            indexArray++;
        }

//        //initialize and fill activity attributes
//        for(int dim = 0; dim < inputMap.size(); dim++)
//        {
//            //initialize correct number of instances
//            Attribute current = new Attribute("Attribute" + dim, dim);
//            if(dim == 0) {
//                for(int obj = 0; obj < inputMap.get("v0").size(); obj++) {
//                    instances.add(new DenseInstance(inputMap.size()));
//                }
//            }
//
//            //fill instances depending on input type
//            for(int obj = 0; obj < inputMap.get("v0").size(); obj++) {
//                if (inputMap.get("v"+dim).get(obj) instanceof Double) {
//                    double i = (Double)inputMap.get("v"+dim).get(obj);
//                    instances.get(obj).setValue(current, i);
//                } else {
//                    if (inputMap.get("v"+dim).get(obj) instanceof Integer) {
//                        int i = (Integer)inputMap.get("v"+dim).get(obj);
//                        instances.get(obj).setValue(current, i);
//                    } else {
//                        String s = (String)inputMap.get("v"+dim).get(obj);
//                        Double d = Double.parseDouble(s);
//                        instances.get(obj).setValue(current, d);
//                    }
//                }
//            }
//            attributes.add(current);
//        }


        Instances returnDataset = new Instances(datasetTitle, attributes, instances.size());
        for(Instance inst : instances)
            returnDataset.add(inst);

        return returnDataset;
    }

//    private HashMap<Integer,String> orderInMap(double a, double b, double c) {
//        HashMap<Integer,String> orderMap = new HashMap<Integer,String>();
//
//        //get order of input values and link to classifying String
//        if (a >= b) {
//            if (a >= c) {
//                orderMap.put(0, "high");
//                if (b >= c) {
//                    orderMap.put(1, "mid");
//                    orderMap.put(2, "low");
//                } else {
//                    orderMap.put(2, "mid");
//                    orderMap.put(1, "low");
//                }
//            } else {
//                orderMap.put(2, "high");
//                orderMap.put(0, "mid");
//                orderMap.put(1, "low");
//            }
//        } else {
//            if (b >= c) {
//                orderMap.put(1, "high");
//                if (a >= c) {
//                    orderMap.put(0, "mid");
//                    orderMap.put(2, "low");
//                } else {
//                    orderMap.put(2, "mid");
//                    orderMap.put(0, "low");
//                }
//            } else {
//                orderMap.put(2, "high");
//                orderMap.put(1, "mid");
//                orderMap.put(0, "low");
//            }
//        }
//
//        return orderMap;
//    }
//
//    private void initClusterRatio(HashMap<String,Integer> clusterRatio) {
//        for (int i = 0; i < 9; i++) {
//            String group = "A";
//            String postfix = null;
//
//            if (i > 3) group = "G";
//            int m = i % 4;
//            postfix = "" + m;
//            if (m == 3) postfix = "n";
//
//            clusterRatio.put("c"+group+postfix, 0);
//        }
//        //initialize cluster-to-cluster counters
//        for (int i = 0; i < 10; i++) {
//            String n1 = "0";
//            if (i > 2) n1 = "1";
//            if (i > 5) n1 = "2";
//            clusterRatio.put("cG" + n1 + "A" + (i % 3), 0);
//        }
//    }

    @Override
    public InputStream getPMMLInputStream() {
        return null;
    }

    @Override
    public Boolean hasPMML() {
        return false;
    }
}

