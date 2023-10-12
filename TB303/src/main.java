import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;

import io.github.warnotte.obj2gui2.JPanelMagique;

/**
 * @author Warnotte Renaud
 *
 */
public class main
{

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		JavaStreamPlayer player = new JavaStreamPlayer();
		
		W303 w303_a = new W303();
		W303 w303_b = new W303();
		W303 w303_c = new W303();
		
		ArrayList selection = new ArrayList();
		selection.add(w303_a);
		
		JPanelMagique p = JPanelMagique.GenerateJPanelFromSelectionAndBindings(selection, null);
		JFrame frame = new JFrame();
		frame.add(p);
		
		selection = new ArrayList();
		selection.add(w303_b);
		p = JPanelMagique.GenerateJPanelFromSelectionAndBindings(selection, null);
		frame.add(p, BorderLayout.SOUTH);
		selection = new ArrayList();
		selection.add(w303_c);
		p = JPanelMagique.GenerateJPanelFromSelectionAndBindings(selection, null);
		frame.add(p, BorderLayout.NORTH);

		frame.setSize(640, 480);
		frame.setVisible(true);
		
		while(true)
		{
			
			w303_a.run();
			w303_b.run();
			w303_c.run();
			
			float val1 = (float) w303_a.out / 127f;
			float val2 = (float) w303_b.out / 127f;
			float val3 = (float) w303_c.out / 127f;
			float val = val1+val2+val3;
			player.write(new float[]{val});
		}
	}

}
