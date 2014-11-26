package david;


class ColaConLimite{
	public static final int TAM_BUFFER = 16;
	protected int[] _valores = new int[TAM_BUFFER];
	volatile protected int _poner = 0;
	volatile protected int _quitar = 0;
	volatile protected int _numElemens = 0;
	
	synchronized public void pon(int v){
		if(_numElemens == TAM_BUFFER){
			throw new RuntimeException("No hay hueco");
		}
		
		_valores[_poner] = v;
		_poner = (_poner + 1) % TAM_BUFFER; // Analizar esta linea
		++_numElemens;
		
		if(_numElemens == 1){
			notify();
		}
	}
	
	synchronized public int quitar(){
		if(_numElemens == 0){
			throw new RuntimeException("No hay nada que quitar");
		}
		--_numElemens;
		int indiceDevolver = _quitar;
		_quitar = (_quitar + 1) % TAM_BUFFER;
		
		if(_numElemens == TAM_BUFFER - 1){
			notify();
		}
		
		return _valores[indiceDevolver];
	}
	
	synchronized public void ponBloqueante(int v) throws InterruptedException{
		while(_numElemens == TAM_BUFFER){
			wait();
		}
		
		_valores[_poner] = v;
		_poner = (_poner + 1) % TAM_BUFFER;
		++_numElemens;
		
		if(_numElemens == 1){
			notify();
		}
	}
	
	synchronized public int quitaBloqueante() throws InterruptedException{
		while (_numElemens == 0) {
			wait();
		}
		
		--_numElemens;
		int indiceDevolver = _quitar;
		_quitar = (_quitar + 1) % TAM_BUFFER;
		
		if(_numElemens == TAM_BUFFER - 1){
			notify();
		}
		
		return _valores[indiceDevolver];
	}
	
	public boolean vacia() {
		return _numElemens == 0;
	}
	
	public boolean estaLlena() {
		return _numElemens == TAM_BUFFER;
	}
	
	public int numElemens(){
		return _numElemens;
	}
}

class Productor implements Runnable{
	private ColaConLimite _cl;
	
	public Productor(ColaConLimite cl) {
		_cl = cl;
	}
	
	public void run(){
		int i = 0;
		boolean reintentar = false;
		
		while(true){
			if(!reintentar){
				++i;
			}
			
			reintentar = false;
			//_dc.mutex.acquireUninterruptibly();
			try {
				_cl.pon(i);
			} catch (Exception e) {
				System.out.println("Lleno");
				reintentar = true;
			}
			//_dc.mutex.release();
		}
	}
}

class Consumidor implements Runnable{
	private ColaConLimite _cl;
	
	public Consumidor(ColaConLimite cl){
		_cl = cl;
	}
	
	public void run(){
		int anterior = 0;
		int nuevo;
		
		while(true){
			//_dc.mutex.acquireUninterruptibly();
			try {
				nuevo = _cl.quitar();
			} catch (Exception e) {
				System.out.println("Vacio");
				//_dc.mutex.release();
				continue;
			}
			//_dc.mutex.release();
			if((nuevo % 1000) == 0) System.out.println(" "+nuevo);
			if (anterior != (nuevo - 1)) {
				System.out.println("NOS HEMOS SALTADO 1");
				System.exit(1);
			}
			anterior = nuevo;
		}
	}
}

public class ProductorConsumidorFinal {

	public static void main(String[] args) {
		ColaConLimite datos = new ColaConLimite();
		
		Productor p = new Productor(datos);
		Consumidor c = new Consumidor(datos);
		
		new Thread(p, "Productor").start();
		new Thread(c, "Consumidor").start();

	}

}

