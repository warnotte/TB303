import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;


public class JavaStreamPlayer implements StreamPlayerInterface
{

	// Deplacer line vers une classe speciale de sortie qui aurait une interface SOUNDOUTPUT avec une methode put();
	private AudioFormat	format;
	SourceDataLine line;
	
	 float rate = 44100.0f/2;
     int channels = 1;
     int sampleSize = 16;

	public JavaStreamPlayer()
	{
		 // create a line to play to
  
   	   
        format = new AudioFormat(rate, sampleSize, channels, true, false);
   	   
       try {
         DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
         line = (SourceDataLine) AudioSystem.getLine(info);
         line.open(format, 4096);
       //  line.open(format);
            
       } catch (LineUnavailableException ex) {
         ex.printStackTrace();
         return;
       }
       
       // start the line
       line.start();
	}
	
	@Override
	public void write(byte[] flux)
	{
		line.write(flux, 0, flux.length);
	}

	public void write(float[] ffM) {

		byte out [] = new byte[ffM.length*2];
		
		FloatSampleTools.float2byteGeneric(ffM, 0, out, 0, 2, ffM.length, format, 64);

		line.write(out , 0, out.length);
		
	}

}
