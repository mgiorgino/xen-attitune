import java.io.Serializable;


public class SizeableObject implements Serializable {

	private static final long serialVersionUID = -8716644838547617037L;

	private byte[] baos;

	public SizeableObject(int sizeInBytes){
		baos = new byte[sizeInBytes];
		for(int i = 0; i < sizeInBytes; i++){
			baos[i]=(byte)55;
		}
	}

	public byte[] getByteArrayOutputStream(){
		return baos;
	}

}
