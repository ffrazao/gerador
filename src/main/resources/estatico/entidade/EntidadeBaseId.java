package aqui;

public interface EntidadeBaseId<Id> extends EntidadeBase {
	
	Id getId();
	
	void setId(Id id);

}
