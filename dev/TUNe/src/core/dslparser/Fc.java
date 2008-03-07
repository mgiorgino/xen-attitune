package core.dslparser;

import java.lang.reflect.Method;

import core.util.TuneException;


public class Fc
{
	public String name,methode,suffixe;
	Method compare;
	Object [] arg;
	
	public Fc(String name, String type, String match) throws TuneException
	{
		this.name=name;
		this.methode=type;
		this.suffixe=match;
		
		// pour gagner du temps, on va chercher la méthode de comparaison une fois pour toute pour cet objet
		compare=getMethod();
	}
	
	public Method getMethod() throws TuneException
	{
		arg=new Object[1];
		arg[0]=suffixe;

		try
		{
			return String.class.getMethod(methode, String.class);
		}
		catch(NoSuchMethodException e)
		{
			
		}
		
		// ok, peut être dans la classe mère Object (getMethod ne donne pas accès aux méthodes héritées
		// à la différence de getMethods
		try
		{
			return String.class.getMethod(methode,Object.class);
		}
		catch(NoSuchMethodException e)
		{
			// cette fois-ci, c'est fini
			throw new TuneException("Parsing XML file - Fc.getMethod() - NoSuchMethodException: "+methode);
		}
		
	}
	
	public boolean match(String test) throws Exception
	{
		// renvoi vrai si la chaine passée correspond à cette interface
		return ((Boolean)compare.invoke(test, arg)).booleanValue();
	}
	
	public boolean equals(Object o)
	{
		Fc a_comparer=(Fc)o;
		return a_comparer.name.equals(name) && a_comparer.methode.equals(methode) && a_comparer.suffixe.equals(suffixe);
	}

}
