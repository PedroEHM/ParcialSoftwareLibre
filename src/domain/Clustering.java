/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author pedro
 */
public class Clustering {
    
    private final String CLUSTERING_FILE_PATH = "BoliviaBMI.arff";
    public Instances instances;
    public SimpleKMeans skm;
    public int numClusters;

    public Clustering() {
        this.numClusters = 2;
    }
    
    public class ModelData {
        public int x[];
        public double y[];
        public int asig[];

        public ModelData(int[]x, double[]y, int[]asig) {
            this.x = x;
            this.y = y;
            this.asig = asig;
        }
    }
    
    public void buildModel(int numClusters) {
       try{
            this.instances = new Instances(new BufferedReader(new FileReader(this.CLUSTERING_FILE_PATH)));
            this.skm = new SimpleKMeans();
            this.skm.setNumClusters(numClusters);
            this.skm.setPreserveInstancesOrder(true);
            this.skm.buildClusterer(this.instances);
            this.numClusters = numClusters;
             
        }catch(Exception e){
            System.out.println(e);
        } 
    }
    
    public ChartPanel generateGraph(){
        XYDataset dataset = createDataset();
        JFreeChart chart=ChartFactory.createScatterPlot("Año vs BMI","Año", "BMI", dataset);
        ChartPanel cp=new ChartPanel(chart);
        return cp; 
    }
    
    private XYDataset createDataset(){
        ModelData md = this.fillData();
        if (md == null) {
            return null;
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series[] = new XYSeries[this.numClusters];
        for (int i=0; i<this.numClusters; i++) {
            series[i] = new XYSeries("C"+(i+1));
        }
        
        for (int i = 0; i < this.instances.numInstances(); i++) {
            int cluster = md.asig[i];
            series[cluster].add(md.x[i],md.y[i]);            
        }
        
        for ( int i=0; i<series.length; i++) {
            dataset.addSeries(series[i]);
        }       
        
        return dataset;
    }
    
    private ModelData fillData(){
        try {
            int x[] =  new int[this.instances.numInstances()];
            double y[] = new double[this.instances.numInstances()];
            int asig[] = skm.getAssignments();
            for(int i=0; i<this.instances.numInstances(); i++) {
                Instance temp = this.instances.instance(i);
                x[i] = Integer.parseInt(temp.toString(0));
                y[i] = Double.parseDouble(temp.toString(1));
            }
            return new ModelData(x, y, asig);
        } catch (Exception ex) {
            Logger.getLogger(Clustering.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    @Override
    public String toString(){
        int num[] = this.skm.getClusterSizes();                
        Instances centroides = skm.getClusterCentroids();
        String cad = "";
        for(int i=0; i<centroides.numInstances(); i++) {
            Instance temp = centroides.instance(i);
            cad = cad+"===================\n";
            cad = cad+"Cent: "+i+"\n";
            cad = cad+"Año: "+temp.toString(0)+"\n";
            cad = cad+"BMI: "+temp.toString(1)+"\n";
            cad = cad+"Instancias: "+num[i]+"\n";
            cad = cad+"% Instancias: "+(num[i] * 100)/this.instances.numInstances()+"%\n";
        }
        return cad;
    }
    
    public String getAsignment() {
        int asig[];
        try {
            asig = skm.getAssignments();
            String cad ="";
            for(int i=0; i<asig.length; i++) {
                Instance temp = this.instances.instance(i);
                cad = cad+"===================\n";
                cad = cad+"Año: "+temp.toString(0)+"\n";
                cad = cad+"BMI: "+temp.toString(1)+"\n";
                cad = cad+"Cluster: "+asig[i]+"\n";
            }
            return cad;
        } catch (Exception ex) {
            Logger.getLogger(Clustering.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
}
