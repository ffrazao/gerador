package entidade;

public interface FiltroDto extends Dto {

	Integer getPagina();

	Integer getTamanho();

	void setPagina(Integer pagina);

	void setTamanho(Integer tamanho);

}
