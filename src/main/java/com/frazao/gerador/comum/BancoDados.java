package com.frazao.gerador.comum;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.exception.JDBCConnectionException;

public class BancoDados {

	private Connection connection;

	private InformacaoConexao informacaoConexao;

	public BancoDados(InformacaoConexao informacaoConexao) {
		this.informacaoConexao = informacaoConexao;
	}

	private void captarAcessorios(DatabaseMetaData meta, DefinicaoTabela tabelaDf) throws SQLException {
		String esquema = tabelaDf.getNomeEsquema();
		String tabela = tabelaDf.getNomeTabela();

		ResultSet keys = null;
		switch (this.informacaoConexao.getPlataformaBanco()) {
		default:
		case MYSQL:
			keys = meta.getExportedKeys(esquema, null, tabela);
			break;
		case POSTGRES:
			keys = meta.getExportedKeys(null, esquema, tabela);
			break;
		}

		while (keys.next()) {
			String esquemaFk = keys.getString("fktable_cat") != null ? keys.getString("fktable_cat")
					: keys.getString("fktable_schem");
			String tabelaFk = keys.getString("fktable_name");
			String colunaFk = keys.getString("fkcolumn_name");

			tabelaDf.addDadosAcessorios(new DefinicaoEstruturaDados(esquemaFk, tabelaFk, colunaFk));
		}
	}

	private void captarSequences(DatabaseMetaData meta, DefinicaoTabela tabelaDf) throws SQLException {
		String esquema = tabelaDf.getNomeEsquema();
		String tabela = tabelaDf.getNomeTabela();

		ResultSet keys = null;
		switch (this.informacaoConexao.getPlataformaBanco()) {
		default:
		case MYSQL:
			keys = meta.getTables(esquema, null, null, new String[] { "SEQUENCE" });
			break;
		case POSTGRES:
			keys = meta.getTables(null, esquema, null, new String[] { "SEQUENCE" });
			break;
		}

		while (keys.next()) {

			for (int i = 1; i <= keys.getMetaData().getColumnCount(); i++) {
				System.out.println(keys.getMetaData().getColumnName(i) + " = " + keys.getString(i));
			}
			System.exit(-1);

//			String esquemaPk = keys.getString("pktable_cat") != null ? keys.getString("pktable_cat")
//					: keys.getString("pktable_schem");
//			String tabelaPk = keys.getString("pktable_name");
//			String colunaPk = keys.getString("pkcolumn_name");
//
//			String colunaFk = keys.getString("fkcolumn_name");
//
//			// encontrar a coluna
//			List<DefinicaoEstruturaDados> dbdPkList = tabelaDf.getEstruturaList().stream()
//					.filter(l -> l.getColuna().equals(colunaFk)).collect(Collectors.toList());
//
//			if (!dbdPkList.isEmpty()) {
//				// garantir que cada coluna só possua uma única referência externa
//				if (dbdPkList.get(0).getReferencia() != null) {
//					throw new RuntimeException("Dados inconsistentes!");
//				}
//				dbdPkList.get(0).setReferencia(new DefinicaoEstruturaDados(esquemaPk, tabelaPk, colunaPk));
//			}
		}
	}

	private void captarFk(DatabaseMetaData meta, DefinicaoTabela tabelaDf) throws SQLException {
		String esquema = tabelaDf.getNomeEsquema();
		String tabela = tabelaDf.getNomeTabela();

		ResultSet keys = null;
		switch (this.informacaoConexao.getPlataformaBanco()) {
		default:
		case MYSQL:
			keys = meta.getImportedKeys(esquema, null, tabela);
			break;
		case POSTGRES:
			keys = meta.getImportedKeys(null, esquema, tabela);
			break;
		}

		while (keys.next()) {
			String esquemaPk = keys.getString("pktable_cat") != null ? keys.getString("pktable_cat")
					: keys.getString("pktable_schem");
			String tabelaPk = keys.getString("pktable_name");
			String colunaPk = keys.getString("pkcolumn_name");

			String colunaFk = keys.getString("fkcolumn_name");

			// encontrar a coluna
			List<DefinicaoEstruturaDados> dbdPkList = tabelaDf.getEstruturaList().stream()
					.filter(l -> l.getColuna().equals(colunaFk)).collect(Collectors.toList());

			if (!dbdPkList.isEmpty()) {
				// garantir que cada coluna só possua uma única referência externa
				if (dbdPkList.get(0).getReferencia() != null) {
					throw new RuntimeException("Dados inconsistentes!");
				}
				dbdPkList.get(0).setReferencia(new DefinicaoEstruturaDados(esquemaPk, tabelaPk, colunaPk));
			}
		}
	}

	private void captarPk(DatabaseMetaData meta, DefinicaoTabela tabelaDf) throws SQLException {
		String esquema = tabelaDf.getNomeEsquema();
		String tabela = tabelaDf.getNomeTabela();

		ResultSet keys = null;

		switch (this.informacaoConexao.getPlataformaBanco()) {
		default:
		case MYSQL:
			keys = meta.getPrimaryKeys(esquema, null, tabela);
			break;
		case POSTGRES:
			keys = meta.getPrimaryKeys(null, esquema, tabela);
			break;
		}

		while (keys.next()) {
			String colunaPk = keys.getString("COLUMN_NAME");

			// encontrar a coluna
			List<DefinicaoEstruturaDados> dbdPkList = tabelaDf.getEstruturaList().stream()
					.filter(l -> l.getColuna().equals(colunaPk)).collect(Collectors.toList());

			// marcar campos que são chaves primárias
			for (DefinicaoEstruturaDados coluna : dbdPkList) {
				coluna.addPropriedade("PK", true);
			}
		}
	}

	public synchronized Connection conectar() {
		if (this.connection == null) {
			try {
				Class.forName(this.informacaoConexao.getDriver());
				this.connection = DriverManager.getConnection(this.informacaoConexao.getUrl(),
						this.informacaoConexao.getUsername(), this.informacaoConexao.getPassword());
			} catch (SQLException | ClassNotFoundException e) {
				throw new RuntimeException("Problemas ao tentar conexão ao Banco de Dados", e);
			}
		}
		return this.connection;
	}

	public List<DefinicaoTabela> getDefinicaoBancoDados(Set<String> filtroSet, Set<String[]> excluiFiltroSet)
			throws Exception {
		return this.getDefinicaoBancoDados(filtroSet, excluiFiltroSet, true, true);
	}

	private List<DefinicaoTabela> getDefinicaoBancoDados(Set<String> filtroSet, Set<String[]> excluiFiltroSet,
			boolean captarPropriedades, boolean captarRelacionamentos) throws Exception {

		final List<DefinicaoTabela> result = new ArrayList<>();

		conectar();

		final AtomicReference<DefinicaoEstruturaDados> dbd = new AtomicReference<>(null);
		final AtomicReference<String> esquema = new AtomicReference<>(null);
		final AtomicReference<String> tabela = new AtomicReference<>(null);
		final AtomicReference<String> coluna = new AtomicReference<>(null);
		final AtomicReference<String> esquemaAnt = new AtomicReference<>(null);
		final AtomicReference<String> tabelaAnt = new AtomicReference<>(null);

		final DatabaseMetaData meta = this.connection.getMetaData();

		final AtomicReference<List<DefinicaoEstruturaDados>> lote = new AtomicReference<>(new ArrayList<>());

		final Runnable captaDefinicaoTabela = () -> {
			if (dbd.get() != null) {
				esquemaAnt.set(esquema.get());
				tabelaAnt.set(tabela.get());
				// captar as referencias externas
				DefinicaoTabela dt = new DefinicaoTabela(lote.get());
				result.add(dt);
				if (captarRelacionamentos) {
					try {
						captarPk(meta, dt);
						captarFk(meta, dt);
						captarAcessorios(meta, dt);
						// captarSequences(meta, dt);
					} catch (SQLException e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}
				// limpar o lote
				lote.set(new ArrayList<>());
			}
		};

		// buscar pelos filtros informados
		for (String filtro : filtroSet) {

			// capturar meta dados solicitados
			String[] filtroTmp = filtro.split("\\.");
			String esquemaFltr = filtroTmp.length >= 1 && filtroTmp[0].trim().length() > 1 ? filtroTmp[0].trim() : null;
			String tabelaFltr = filtroTmp.length >= 2 && filtroTmp[1].trim().length() > 1 ? filtroTmp[1].trim() : null;
			String colunaFltr = filtroTmp.length >= 3 && filtroTmp[2].trim().length() > 1 ? filtroTmp[2].trim() : "%";
			ResultSet metaReg = null;
			switch (this.informacaoConexao.getPlataformaBanco()) {
			default:
			case MYSQL:
				metaReg = meta.getColumns(esquemaFltr, null, tabelaFltr, colunaFltr);
				break;
			case POSTGRES:
				metaReg = meta.getColumns(null, esquemaFltr, tabelaFltr, colunaFltr);
				break;
			}

			// se meta dado encontrado
			if (metaReg != null) {
				esquema.set(null);
				tabela.set(null);
				coluna.set(null);
				esquemaAnt.set(null);
				tabelaAnt.set(null);

				dbd.set(null);

				// percorrer o meta dado encontrado
				fora: while (metaReg.next()) {
					esquema.set(metaReg.getString(1) == null ? metaReg.getString(2) : metaReg.getString(1));
					tabela.set(metaReg.getString(3));
					coluna.set(metaReg.getString(4));

					// verificar se o registro deve ser IGNORADO
					for (String[] f : excluiFiltroSet) {
						if ((f.length == 3
								&& (f[0] == null || f[0].isBlank() || f[0].equals("%") || f[0].equals(esquema.get()))
								&& (f[1] == null || f[1].isBlank() || f[1].equals("%") || f[1].equals(tabela.get()))
								&& (f[2] == null || f[2].isBlank() || f[2].equals("%") || f[2].equals(coluna.get())))
								|| (f.length == 2
										&& (f[0] == null || f[0].isBlank() || f[0].equals("%")
												|| f[0].equals(esquema.get()))
										&& (f[1] == null || f[1].isBlank() || f[1].equals("%")
												|| f[1].equals(tabela.get())))
								|| (f.length == 1 && (f[0] == null || f[0].isBlank() || f[0].equals("%")
										|| f[0].equals(esquema.get())))) {
							continue fora;
						}
					}

					// dar sequencia na pesquisa
					if (esquemaAnt.get() == null) {
						esquemaAnt.set(esquema.get());
						tabelaAnt.set(tabela.get());
					}

					// montar definição da estrutura de dados (Registro)
					dbd.set(new DefinicaoEstruturaDados(esquema.get(), tabela.get(), coluna.get()));
					if (captarPropriedades) {
						// captar demais propriedades do campo
						ResultSetMetaData campos = metaReg.getMetaData();
						for (int pos = 1; pos <= campos.getColumnCount(); pos++) {
							dbd.get().addPropriedade(campos.getColumnName(pos), metaReg.getString(pos));
						}
					}

					// verificar se modificou a tabela
					if (!esquemaAnt.get().equals(esquema.get()) || !tabelaAnt.get().equals(tabela.get())) {
						// materializar estrutura de tabela
						captaDefinicaoTabela.run();
					}

					lote.get().add(dbd.get());
				}
				// materializar estrutura de tabela
				captaDefinicaoTabela.run();
			}
		}

		return result;
	}

	// para testes futuros se necessário
	private void printAllSequenceName(Connection conn) throws JDBCConnectionException, SQLException {
		DialectResolver dialectResolver = new StandardDialectResolver();
		Dialect dialect = dialectResolver
				.resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(conn.getMetaData()));

		if (dialect.supportsSequences()) {
			String sql = dialect.getQuerySequencesString();
			if (sql != null) {

				Statement statement = null;
				ResultSet rs = null;
				try {
					statement = conn.createStatement();
					rs = statement.executeQuery(sql);

					while (rs.next()) {
						ResultSetMetaData md = rs.getMetaData();
						StringBuffer sb = new StringBuffer();
						for (int i = 1; i <= md.getColumnCount(); i++) {
							sb.append(rs.getString(i)).append(", ");
						}
						System.out.println("Sequence Name : " + sb.toString());
					}
				} finally {
					if (rs != null)
						rs.close();
					if (statement != null)
						statement.close();
				}

			}
		}
	}
}
