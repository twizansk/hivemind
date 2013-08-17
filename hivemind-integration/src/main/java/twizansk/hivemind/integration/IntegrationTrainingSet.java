package twizansk.hivemind.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;

public class IntegrationTrainingSet implements ITrainingSet {

	private BufferedReader reader;
	
	public IntegrationTrainingSet() throws Exception {
		this.reset();
	}
	
	@Override
	public TrainingSample getNext() {
		try {
			String line = reader.readLine();
			if (line == null) {
				return null;
			}
			String[] strs = line.split(", ");
			double[] x = new double[strs.length - 1];
			for (int i =0; i < x.length; i++) {
				x[i] = Double.parseDouble(strs[i]);
			}
			double y = Double.parseDouble(strs[strs.length - 1]);
			return new TrainingSample(x, y);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void reset() {
		try {
			if (reader != null) {
				reader.close();
			}
			URI uri = ClassLoader.getSystemResource("linear-regression.csv").toURI();
			Charset charset = Charset.forName("US-ASCII");
			Path file = Paths.get(uri.getPath());
			reader = Files.newBufferedReader(file, charset);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
