package edu.lu.uni.dataset.cnn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.util.FileHelper;

public class DeepLearningWithCNN2 {

	private static Logger log = LoggerFactory.getLogger(DeepLearningWithCNN2.class);
	
	private static final String INPUT_DATA_FILE_PATH = "/data-for-CNN2/";
	
	public static void main(String[] args) throws IOException, InterruptedException {
//		String fileName = "apache$commons-math$feature-only-ast-node-name(RAW_CAMEL_TOKENIATION)MaxSize=2791-9.csv";
		String fileName = "apache$commons-math$feature-only-ast-node-name(RAW_CAMEL_TOKENIATION)MaxSize=2791-6406.csv";
		int indexOfMaxSize = fileName.lastIndexOf("MaxSize=");
		String[] maxSizes = fileName.substring(indexOfMaxSize + "MaxSize=".length(), fileName.lastIndexOf(".csv")).split("-"); 
		int labelIndex = Integer.parseInt(maxSizes[0]);
		int numClasses = Integer.parseInt(maxSizes[1]);     
		
        int batchSizeTraining = 642;
//        DataSetIterator trainingData = readCSVDataset(INPUT_DATA_FILE_PATH + fileName, batchSizeTraining, labelIndex, labelIndex + numClasses - 1, numClasses);
        DataSet trainingData = readCSVDataset2(INPUT_DATA_FILE_PATH + fileName, batchSizeTraining, labelIndex, numClasses);

        // this is the data we want to classify
        int batchSizeTest = 642;
//        DataSetIterator testData = readCSVDataset(INPUT_DATA_FILE_PATH + fileName, batchSizeTraining, labelIndex, labelIndex + numClasses - 1, numClasses);
        DataSet testData = readCSVDataset2(INPUT_DATA_FILE_PATH + fileName, batchSizeTraining, labelIndex, numClasses);

        //We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit variance):
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(trainingData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainingData);     //Apply normalization to the training data
        normalizer.transform(testData);         //Apply normalization to the test data. This is using statistics calculated from the *training* set

        final int numInputs = labelIndex;
        int outputNum = numClasses;
        int iterations = 1;
        long seed = 600;
        int kernelSizeOfCon = 3;
        int kernelSizeOfSub = 2;
        int numberOfFeatures = 100;
        int nEpochs = 1;
        
        log.info("Build model....");
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations) // Training iterations as above
                .regularization(true).l2(0.0005)
                .learningRate(0.001) // [0.001, 0.1], smaller steps, longer training times, more precise results.
//                .biasLearningRate(0.02) 
//                .learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.5) // accuracy <-- momentum --> speed
                .list()
                .layer(0, new ConvolutionLayer.Builder(1, kernelSizeOfCon)
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(1)
                        .stride(1, 1)
                        .nOut(10)
                        .activation("identity")
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(1,kernelSizeOfSub)
                        .stride(1,2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(1, kernelSizeOfCon)
                        //Note that nIn need not be specified in later layers
                        .stride(1, 1)
                        .nOut(20)
                        .activation("identity")
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(1,kernelSizeOfSub)
                        .stride(1,2)
                        .build())
                .layer(4, new DenseLayer.Builder().activation("softmax")
                        .nOut(numberOfFeatures).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.MEAN_ABSOLUTE_ERROR)
                        .nOut(numClasses)
                        .activation("softmax")
                        .build())
                .setInputType(InputType.convolutionalFlat(1,numInputs,1)) //See note below
                .backprop(true).pretrain(false);

        MultiLayerConfiguration conf = builder.build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(1));

        log.info("Train model....");
        for( int i=0; i<nEpochs; i++ ) {
    		model.fit(trainingData);
        }
        log.info("****************Example finished********************");

        //evaluate the model on the test set
        List<String> l = new ArrayList<>();
        Evaluation eval = new Evaluation(l);
        DataSet test = testData;
        INDArray output = model.output(test.getFeatureMatrix());

        eval.eval(test.getLabels(), output);
        log.info(eval.stats());


	}
	
	private static DataSetIterator readCSVDataset(
            String csvFileClasspath, int batchSize, int labelIndexFrom, int labelIndexTo, int numClasses)
            throws IOException, InterruptedException{

    	RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new ClassPathResource(csvFileClasspath).getFile()));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr,batchSize,labelIndexFrom, labelIndexTo,true);
        return iterator;
    }
	
	private static DataSet readCSVDataset2(
            String csvFileClasspath, int batchSize, int labelIndex, int numClasses)
            throws IOException, InterruptedException{

    	RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new ClassPathResource(csvFileClasspath).getFile()));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr,batchSize,labelIndex,numClasses);
        return iterator.next();
    }

}
