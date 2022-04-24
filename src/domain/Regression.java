/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
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
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author pedro
 */
public class Regression {

    private final String REGRESION_FILE_PATH = "BoliviaLifeExp.arff";
    private final int CLASS_INDEX = 1;
    private final int NUM_FOLDS = 10;
    public Instances instances;
    public LinearRegression lr;
    public Evaluation ev;
    private boolean built;
    
    public class ModelData {
        public int x[];
        public double y[];
        public double coef[];

        public ModelData(int[]x, double[]y, double[]coef) {
            this.x = x;
            this.y = y;
            this.coef = coef;
        }
    }
    
    public Regression() {
        this.built = false;
    }
    
    public void buildModel() {
       try{
            this.instances = new Instances(new BufferedReader(new FileReader(this.REGRESION_FILE_PATH)));
            this.instances.setClassIndex(this.CLASS_INDEX);            
            this.lr = new LinearRegression();
            this.lr.buildClassifier(instances);
            
            this.ev = new Evaluation(this.instances);
            this.ev.crossValidateModel(lr, instances, this.NUM_FOLDS, new Random(this.NUM_FOLDS), new String[]{});   
            this.built = true;
             
        }catch(Exception e){
            System.out.println(e);
        } 
    }
    
    public double predict(int year) {
        double anio=year;
        double arrCoef[] = lr.coefficients();
        return anio *  arrCoef[0] + arrCoef[2];
    }
    
    
    public ChartPanel generateGraph(){
        XYDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Años vs EV",
            "Años",
            "Expectativa de vida (EV)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            false,
            false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesVisible(2, false);
        plot.setRenderer(renderer);
        ChartPanel cp=new ChartPanel(chart);
        return cp; 
    }
    
    private XYDataset createDataset(){
        ModelData md = this.fillData();
        
        XYSeries series1 = new XYSeries("Datos");
        XYSeries series2 = new XYSeries("Regresión");
        
        for (int i = 0; i <md.x.length; i++) {
            series1.add(md.x[i], md.y[i]);
            double y_mod=md.x[i]*md.coef[0]+md.coef[2];
            series2.add(md.x[i], y_mod);
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        return dataset;
    }
    
    private ModelData fillData(){
        int x[] =  new int[this.instances.numInstances()];
        double y[] = new double[this.instances.numInstances()];
        double coef[] = lr.coefficients();
        for(int i=0; i<this.instances.numInstances(); i++) {
            Instance temp = this.instances.instance(i);
            x[i] = Integer.parseInt(temp.toString(0));
            y[i] = Double.parseDouble(temp.toString(1));
        }
        return new ModelData(x, y, coef);
    }
    
    public boolean isBuilt(){
        return this.built;
    }
    
    @Override
    public String toString(){
        String text = "";
        double []coef = lr.coefficients();
        text += "Modelo: Linear Regresion Model \n\nlife_expectancy = " + coef[0] + " * año + "+coef[2] + "\n";
        text += "Evaluación: " + ev.toSummaryString();
        return text;
    }
}
