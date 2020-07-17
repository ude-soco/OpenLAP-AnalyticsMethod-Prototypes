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

public class Kmean2NumericRaw extends AnalyticsMethod {
    public Kmean2NumericRaw() {
        this.setInput(new OpenLAPDataSet());
        this.setOutput(new OpenLAPDataSet());
        this.setParams(new OpenLAPDynamicParams());

        try {
//            this.getInput().addOpenLAPDataColumn(
//                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_common", OpenLAPColumnDataType.STRING, true, "Users", "List of users")
//            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_1", OpenLAPColumnDataType.Numeric, true, "Numeric Attribute 1", "Numeric attribute 1")
            );
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_2", OpenLAPColumnDataType.Numeric, true, "Numeric Attribute 2", "Numeric attribute 2")
            );

            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_1", OpenLAPColumnDataType.Numeric, true, "Numeric Attribute 1", "Numeric attribute 1")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("attribute_2", OpenLAPColumnDataType.Numeric, true, "Numeric Attribute 2", "Numeric attribute 2")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("cluster_name", OpenLAPColumnDataType.Text, true, "Cluster ID (E.g. Cluster 1, Cluster 2)", "The cluster id for each item in the format Clutser 1, Cluster 2")
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
        return "Create clusters based on two numeric attributes using K-Means";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Generate cluster using two numeric attributes using the K-Means algorithm and return the attributes along with the name of their cluster.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    @Override
    protected void implementationExecution() {
        try {
            //ArrayList<String> attribute_common = this.getInput().getColumns().get("attribute_common").getData();
            ArrayList<Double> attribute_1 = this.getInput().getColumns().get("attribute_1").getData();
            ArrayList<Double> attribute_2 = this.getInput().getColumns().get("attribute_2").getData();

            int cluster_count = (Integer)this.getParams().getParams().get("cluster_count").getValue();
            int kmean_seed = (Integer)this.getParams().getParams().get("kmean_seed").getValue();
            int kmean_iterations = (Integer)this.getParams().getParams().get("kmean_iterations").getValue();

            HashMap<String, ArrayList> activity = new HashMap<String, ArrayList>();
            //activity.put("v0", attribute_common);
            activity.put("dou-v0", attribute_1);
            activity.put("dou-v1", attribute_2);

//            HashMap<String, ArrayList> grades = new HashMap<String, ArrayList>();
//            ArrayList<String> attribute_common_clone = (ArrayList) attribute_common.clone();
//            grades.put("v0", attribute_common_clone);
//            grades.put("v1", attribute_double_2);

            Instances activityDataset = createInstance(activity, "activitiesDataset");
            //Instances gradesDataset = createInstance(grades, "gradesDataset");

            //initialize clusterers and filtered clusterers
            FilteredClusterer fcActivity = new FilteredClusterer();
            //FilteredClusterer fcGrades = new FilteredClusterer();

            SimpleKMeans kmeansActivity = new SimpleKMeans();
            //SimpleKMeans kmeansGrades = new SimpleKMeans();

            //remove options used to remove user ID from being clustered
            //String[] options = weka.core.Utils.splitOptions("-R 1");

            //setup of filter on clustering method
//            Remove removeActivity = new Remove();
//            removeActivity.setOptions(options);
//            removeActivity.setInputFormat(activityDataset);
//            fcActivity.setFilter(removeActivity);
            fcActivity.setClusterer(kmeansActivity);

//            Remove removeGrades = new Remove();
//            removeGrades.setOptions(options);
//            removeGrades.setInputFormat(gradesDataset);
//            fcGrades.setFilter(removeGrades);
//            fcGrades.setClusterer(kmeansGrades);


            //set up kmeans clustering
            kmeansActivity.setSeed(kmean_seed);
            kmeansActivity.setPreserveInstancesOrder(true);
            kmeansActivity.setNumClusters(cluster_count);
            kmeansActivity.setMaxIterations(kmean_iterations);



//            kmeansGrades.setSeed(kmean_seed);
//            kmeansGrades.setPreserveInstancesOrder(true);
//            kmeansGrades.setNumClusters(cluster_count);

            //cluster datasets
            fcActivity.buildClusterer(activityDataset);
            //fcGrades.buildClusterer(gradesDataset);

            //kmeansActivity.buildClusterer(activityDataset);

            for (int i = 0; i < activityDataset.numInstances(); i++) {
                Instance inst = activityDataset.instance(i);
                int x = kmeansActivity.clusterInstance(inst);
                x += 1;

                //System.out.println( x + " - " + inst.toString());

                getOutput().getColumns().get("cluster_name").getData().add("Cluster " + x);
            }
            getOutput().getColumns().get("attribute_1").setData(attribute_1);
            getOutput().getColumns().get("attribute_2").setData(attribute_2);


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

    private HashMap<Integer,String> orderInMap(double a, double b, double c) {
        HashMap<Integer,String> orderMap = new HashMap<Integer,String>();

        //get order of input values and link to classifying String
        if (a >= b) {
            if (a >= c) {
                orderMap.put(0, "high");
                if (b >= c) {
                    orderMap.put(1, "mid");
                    orderMap.put(2, "low");
                } else {
                    orderMap.put(2, "mid");
                    orderMap.put(1, "low");
                }
            } else {
                orderMap.put(2, "high");
                orderMap.put(0, "mid");
                orderMap.put(1, "low");
            }
        } else {
            if (b >= c) {
                orderMap.put(1, "high");
                if (a >= c) {
                    orderMap.put(0, "mid");
                    orderMap.put(2, "low");
                } else {
                    orderMap.put(2, "mid");
                    orderMap.put(0, "low");
                }
            } else {
                orderMap.put(2, "high");
                orderMap.put(1, "mid");
                orderMap.put(0, "low");
            }
        }

        return orderMap;
    }

    private void initClusterRatio(HashMap<String,Integer> clusterRatio) {
        for (int i = 0; i < 9; i++) {
            String group = "A";
            String postfix = null;

            if (i > 3) group = "G";
            int m = i % 4;
            postfix = "" + m;
            if (m == 3) postfix = "n";

            clusterRatio.put("c"+group+postfix, 0);
        }
        //initialize cluster-to-cluster counters
        for (int i = 0; i < 10; i++) {
            String n1 = "0";
            if (i > 2) n1 = "1";
            if (i > 5) n1 = "2";
            clusterRatio.put("cG" + n1 + "A" + (i % 3), 0);
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

