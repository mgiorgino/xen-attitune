/***
 * Created 26/02/04
 * Parser of an Apache configuration file (httpd.conf)
 * Contact: Daniel.Hagimont@imag.fr
 * Author: Daniel Hagimont
 * Revised : 
 *   06/07/04 Daniel Hagimont completion of the integration in Jade2
 *   06/01/05 Daniel Hagimont Adaptation to conform to Jade4
 */
package extension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.StringTokenizer;

import core.util.TuneException;

public class KeyValueParser 
{

	final static int LINE_LIMIT = 2000;

	String filename;

	ArrayList buffer;

	public KeyValueParser(String f) 
	{
		filename = f;
		buffer=new ArrayList();
	}

	public void open() throws TuneException 
	{
		RandomAccessFile in;
		String line;
		StringTokenizer st;
		String tok;

		try 
		{
			in = new RandomAccessFile(filename, "r");
			line = in.readLine();

			while (line != null) 
			{
	
				st = new StringTokenizer(line, " \t\n\r", false);
	
				try 
				{
					tok = st.nextToken();
				} 
				catch (Exception ex) 
				{
					try 
					{
						line = in.readLine();
					} catch (IOException e) 
					{
						throw new TuneException("Runtime - KeyValueParser.open() - 2 - "+ e);
					}
					continue;
				}
	
				if (tok.startsWith("#"))
				{
					try 
					{
						line = in.readLine();
					} 
					catch (IOException e) 
					{
						throw new TuneException("Runtime - KeyValueParser.open() - 3 - "+ e);
					}
					continue;
				}
	
				buffer.add(line);
	
				try 
				{
					line = in.readLine();
				} 
				catch (IOException e) 
				{
					throw new TuneException("Runtime - KeyValueParser.open() - 4 - "+ e);
				}
			}
			try 
			{
				in.close();
			} 
			catch (IOException e) 
			{
				throw new TuneException("Runtime - KeyValueParser.open() - 5 - "+ e);
			}
		
		} 
		catch (Exception e) 
		{
			//throw new MonException("Runtime - KeyValueParser.open() - 1 - "+ e);
		}
		


	}
	
	public void view()
	{
		for(int i=0;i<buffer.size();i++)
			System.out.println(buffer.get(i));
			
	}

	public void close() throws TuneException 
	{

		File fname = new File(filename);
		if(fname.exists())
			fname.delete();

		RandomAccessFile output;
		try 
		{
			output = new RandomAccessFile(filename, "rw");
		} 
		catch (FileNotFoundException e) 
		{
			throw new TuneException("Runtime - KeyValueParser.close() - 1 - "+ e);
		}

		for (int i=0;i<buffer.size();i++)
		{
			try 
			{
				output.writeBytes((String)buffer.get(i) + "\n");
			} 
			catch (IOException e) 
			{
				throw new TuneException("Runtime - KeyValueParser.close() - 2 - "+ e);
			}
		}
		
		try
		{
			output.close();
		}
		
		catch (IOException e) 
		{
			throw new TuneException("Runtime - KeyValueParser.close() - 3 - "+ e);
		}

	}

	public void setProperty(String prop, String value, String separator) 
	{
		StringTokenizer st;
		String tok;
		int i;
		boolean found=false;

		for (i = 0; i < buffer.size(); i++) 
		{
			st = new StringTokenizer((String)buffer.get(i), " \t\n\r", false);
			try 
			{
				tok = st.nextToken();
			} 
			catch (Exception ex) 
			{
				continue;
			}
			if (tok.equals(prop)) 
			{
				buffer.set(i, prop + separator + value);
				found=true;
				break;
			}
		}

		if(!found)
			append(prop+separator+value);
	}

	public String getProperty(String prop) 
	{
		StringTokenizer st;
		String tok;
		int i;

		for (i = 0; i < buffer.size(); i++) 
		{
			st = new StringTokenizer((String)buffer.get(i), " \t\n\r", false);
			try 
			{
				tok = st.nextToken();
				if (tok.equals(prop)) 
				{
					tok = st.nextToken();
					return tok;
				}
			} 
			catch (Exception ex) 
			{
				continue;
			}
		}
		return null;
	}

	public void append(String line) 
	{
		buffer.add(line);
	}

	public void substitute(String from, String to) 
	{
		int i;

		for (i = 0; i < buffer.size(); i++) 
		{
			buffer.set(i, ((String)buffer.get(i)).replaceAll(from, to));
		}
	}


}
