import io.github.warnotte.waxlib3.core.TemplatePropertyMerger.property_mode;
import io.github.warnotte.waxlib3.core.TemplatePropertyMerger.Annotations.PROPERTY_interface;
import io.github.warnotte.waxlib3.core.TemplatePropertyMerger.Annotations.PROPERTY_interface.gui_type;

/**
 * @author Warnotte Renaud (original 303 code not from me : Lars Hamre, 1995)
 */
public class W303
{

	float	reso	= 0.05f;
	float	cutoff	= 0.08f;

	int		len		= 2000;

	@PROPERTY_interface(Operation = property_mode.PROPERTY_MERGEABLE, gui_type = gui_type.SLIDER, min = 500, max = 10000, divider = 1)
	public synchronized int getLen()
	{
		return len;
	}

	public synchronized void setLen(int len)
	{
		this.len = len;
	//	o = new float[len];
	}

	@PROPERTY_interface(Operation = property_mode.PROPERTY_MERGEABLE, gui_type = gui_type.FLATSLIDER, min = 1, max = 50, divider = 100)
	public synchronized float getReso()
	{
		return reso;
	}

	public synchronized void setReso(float reso)
	{
		this.reso = reso;

	}

	@PROPERTY_interface(Operation = property_mode.PROPERTY_MERGEABLE, gui_type = gui_type.FLATSLIDER, min = 1, max = 100, divider = 100)
	public synchronized float getCutoff()
	{
		return cutoff;
	}

	public synchronized void setCutoff(float cutoff)
	{
		this.cutoff = cutoff;

	}

	/*
	 * 1 3 6 8 10 13 15 18 20 22 25 27 30 32 34 c d f g a c d f g a c d f g a C
	 * D E F G A B C D E F G A B C D E F G A B C 0 2 4 5 7 9 11 12 14 16 17 19
	 * 21 23 24 26 28 29 31 33 35 36
	 */
	static char	Note[]			= { 24, 23, 19, 17, 19, 7, 21, 22, 10, 20, 8, 18, 17, 8, 20, 12 };

	/*
	 * 0 = rest 1 = 16th note 2 = tie
	 */

	static char	NoteLength[]	= { 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1 };

	/*
	 * 0 = no accent 1 = accent on Accent is not used yet, but increases volume
	 * and resonance
	 */

	char		Accent[]		= { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	/*
	 * 0 = go instantly to the next note 1 = slide to the new note
	 */

	static char	Slide[]			= { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	//float		o[]				= new float[len];

	int tot = 0, out, notenum = 0;
	float vco2 = 0, vco = 1.0f;
	float vcophase = 0;
	float vcofreq = 1, lastvcofreq = 0, vcoadd = 0, vcofreq2 = 0;
	float hig = 0, mid = 0, low = 0, freq = 0;
	float hig2 = 0, mid2 = 0, low2 = 0;

	// Note: resonance is "backwards" in this type of filter, so a low
	// value is max resonance. 

	//float reso = 0.05f;
	float feedbk, flevel = 0.01f;
	float amp = 0.0f, inp;
	int emode = 2;

	public W303() throws Exception
	{
	//	o = new float[len];
	}
	
	public void run()
	{
		//while (true)
		{

			//---------- VCO ----------
			// The squarewave rises faster than is falls, and that means that the
			// rising edge is sharper and will have more resonance. I guess it is
			// caused by the VCO being an integrator with a reset circuit for the
			// sawtooth wave, and the squarewave is simply a "hard limited" version
			// of the saw by feeding it through an op-amp comparator. This simulation
			// only does squarewave, but a sawtooth would be: vcophase/128 - 1.

			if (vco > 0)
				vco2 = vco2 + (vco - vco2) * 0.95f;
			else
				//      vco2 = vco2 + (vco - vco2) * 0.2;
				vco2 = vco2 + (vco - vco2) * 0.9f;

			// Highpass filter to get the "falling down to zero" look.
			// This is probably not what really happens, because the 303 has lots of
			// bass output - so I guess it is caused by the filter. It is just a hack
			// to make the waveform look more like the original. I need a 303 sample
			// without resonance!
			//			{
			//				static float delta=0;
			//				delta += vco2;
			//				delta = delta * 0.99;
			//				inp = delta;
			//				delta -= vco2;
			//			}

			inp = vco2;

			// Update VCO phase

			if (Slide[notenum] != 0)
				vcofreq2 = vcofreq2 + (vcofreq - vcofreq2) * 0.002f;
			else
				vcofreq2 = vcofreq;
			if (vcofreq2 != lastvcofreq)
				vcoadd = (float) Math.pow(2.0, vcofreq2 - 0.37);
			//      vcoadd = pow(2.0, vcofreq2 + 0.35);
			lastvcofreq = vcofreq2;
			vcophase += vcoadd;
			if (vcophase >= 256)
			{
				vcophase -= 256;
				vco = -vco;
			}
			if ((tot % len) == 0)
				vcofreq = Note[notenum] / 12.0f;

			//---------- VCF ----------

			// Initial cutoff freq + envelope amount
			freq = (0.08f + 0.8f * amp) * getCutoff();

			//reso *= 0.99995;
			feedbk = reso * mid;

			/**** Useless (?) hack to simulate diode limiting of resonance ****/
			if (feedbk > flevel)
			{
				float sq = (feedbk - flevel) * 2.0f;
				feedbk += sq * sq;
			} else if (feedbk < -flevel)
			{
				float sq = (feedbk + flevel) * 2.0f;
				feedbk -= sq * sq;
			}

			/******************************************************************/
			// 2 pole filter #1

			hig = inp - feedbk - low;
			mid += hig * freq;
			low += mid * freq;

			// 2 pole filter #2
			hig2 = low - 1 * mid2 - low2;
			mid2 += hig2 * freq;
			low2 += mid2 * freq;

			//---------- VCA ----------

			// Trig envelope attack
			if ((tot % len) == 0)
				if (NoteLength[notenum] == 1)
					emode = 0;

			// Trig envelope release
			if ((tot % len) == 1100)
				if (NoteLength[(notenum + 1) % 16] != 2)
					emode = 2;
			switch (emode)
			{
				case 0:
					// Attack state
					// Capacitor charge attack...
					//amp = amp + (1.1 - amp) * 0.01;

					// ... but the TB303 attack looks more like this

					amp = amp * 1.1f + 0.01f;
					if (amp >= 1.0)
					{
						amp = 1.0f;
						emode = 1;
					}
					break;
				// Decay state - tweak as you like
				case 1:
					amp = amp * 0.9998f;
					break;
				// Release state
				case 2:
					amp = amp * 0.99f;
					break;
			}
			// Output is lowpass (try low or low2) multiplied by amplitude.
			// 40 seems like a nice value to avoid clipping with lots of
			// resonance.
			out = (int) (low * amp * 40f);

			//	System.err.println("Out == "+out);
			//-------- Output ----------

			// Clip to min/max values
			// Easy to tweak to 16 bit output instead of 8

			if (out > 127)
				out = 127;
			if (out < -128)
				out = -128;

		//	o[tot] = (float) out / 127f;
			tot++;

			//------------------------

			// At the end of a note, start another one
			if ((tot % len) == (len - 1))
			{
				notenum++;
				if (notenum >= 16)
					notenum = 0;

				tot = 0;
				
			}
		}
	}

}
