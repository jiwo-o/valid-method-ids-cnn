package edu.lu.uni.deeplearning.VB;

import java.io.FileNotFoundException;
import java.io.IOException;

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
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CNNon_feature_only_ast_node_name {
	
	/**
	 * Number of label classes:
	 * 20 
	 */
	private static Logger log = LoggerFactory.getLogger(CNNon_feature_only_ast_node_name.class);
	private static final String TRAINING_DATA_FILE_PATH = "/data-for-CNN/trainingdata/";
	private static final String TESTING_DATA_FILE_PATH = "/data-for-CNN/testingdata/";
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		String fileName = "apache$commons-math$feature-only-ast-node-name(SIMPLIFIED_NLP)(2)MaxSize=76.csv";
		int indexOfMaxSize = fileName.lastIndexOf("MaxSize=");
		int labelIndex = Integer.parseInt(fileName.substring(indexOfMaxSize + "MaxSize=".length(), fileName.lastIndexOf(".csv")));
		int numClasses = 20;    
		
		int nChannels = 1;   // Number of input channels
        int outputNum = numClasses; // The number of possible outcomes
        int batchSize = 100; // Test batch size
        int nEpochs = 1;     // Number of training epochs
        int iterations = 1;  // Number of training iterations
        int seed = 123;      //

        log.info("Load data....");
        RecordReader trainingDataReader = new CSVRecordReader();
        trainingDataReader.initialize(new FileSplit(new ClassPathResource(TRAINING_DATA_FILE_PATH + fileName).getFile()));
        DataSetIterator trainingData = new RecordReaderDataSetIterator(trainingDataReader,batchSize,labelIndex,numClasses);
        RecordReader testingDataReader = new CSVRecordReader();
        int testBatchSize = 798;
        testingDataReader.initialize(new FileSplit(new ClassPathResource(TESTING_DATA_FILE_PATH + fileName).getFile()));
        DataSetIterator testData = new RecordReaderDataSetIterator(testingDataReader,testBatchSize,labelIndex,numClasses);
        
        /*
         *  Construct the neural network
         */
        log.info("Build model....");
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations) // Training iterations as above
                .regularization(true).l2(0.0005)
                .learningRate(.01)//.biasLearningRate(0.02)
                //.learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new ConvolutionLayer.Builder(1, 5)
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation("identity")
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(1, 5)
                        .stride(1, 1)
                        .nOut(50)
                        .activation("identity")
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(4, new DenseLayer.Builder().activation("relu")
                        .nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.MEAN_ABSOLUTE_ERROR)
                        .nOut(outputNum)
                        .activation("softmax")
                        .build())
                .setInputType(InputType.convolutionalFlat(1,labelIndex,1))
                .backprop(true).pretrain(false);

        MultiLayerConfiguration conf = builder.build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();


        log.info("Train model....");
        model.setListeners(new ScoreIterationListener(1));
        for( int i=0; i<nEpochs; i++ ) {
            model.fit(trainingData);
            log.info("*** Completed epoch {} ***", i);

            log.info("Evaluate model....");
            Evaluation eval = new Evaluation(outputNum);
            while(testData.hasNext()){
                DataSet ds = testData.next();
                INDArray output = model.output(ds.getFeatureMatrix(), false);
                eval.eval(ds.getLabels(), output);

            }
            log.info(eval.stats());
            testData.reset();
        }
        log.info("****************Example finished********************");
	}
}
