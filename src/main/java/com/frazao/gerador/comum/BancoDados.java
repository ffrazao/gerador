package com.frazao.gerador.comum;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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
		String esquema = tabelaDf.getEsquema();
		String tabela = tabelaDf.getTabela();

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
		String esquema = tabelaDf.getEsquema();
		String tabela = tabelaDf.getTabela();

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
		String esquema = tabelaDf.getEsquema();
		String tabela = tabelaDf.getTabela();

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
					if (!dbdPkList.get(0).getReferencia().getEsquema().equals(esquemaPk)
							|| !dbdPkList.get(0).getReferencia().getTabela().equals(tabelaPk)
							|| !dbdPkList.get(0).getReferencia().getColuna().equals(colunaPk)) {
						throw new RuntimeException("Dados inconsistentes! campo fk referenciado mais de uma vez");
					}
				} else {
					dbdPkList.get(0).setReferencia(new DefinicaoEstruturaDados(esquemaPk, tabelaPk, colunaPk));
				}
			} else {
				throw new RuntimeException("Dados inconsistentes campo fk não encontrado!");
			}
		}
	}

	private void captarPk(DatabaseMetaData meta, DefinicaoTabela tabelaDf) throws SQLException {
		String esquema = tabelaDf.getEsquema();
		String tabela = tabelaDf.getTabela();

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

	private synchronized Connection conectar() {
		try {
			if (this.connection == null || this.connection.isClosed()) {
				try {
					Class.forName(this.informacaoConexao.getDriver());
					this.connection = DriverManager.getConnection(this.informacaoConexao.getUrl(),
							this.informacaoConexao.getUsername(), this.informacaoConexao.getPassword());
				} catch (SQLException | ClassNotFoundException e) {
					throw new RuntimeException("Problemas ao tentar conexão ao Banco de Dados", e);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Problemas ao tentar conexão ao Banco de Dados", e);
		}
		return this.connection;
	}

	public List<DefinicaoTabela> getDefinicaoBancoDados() throws Exception {
		return this.getDefinicaoBancoDados(this.informacaoConexao.getFiltroAdicionaList(),
				this.informacaoConexao.getFiltroExcluiList(), true, true);
	}

	private List<DefinicaoTabela> getDefinicaoBancoDados(Collection<String> filtroSet,
			Collection<String> excluiFiltroSet, boolean captarPropriedades, boolean captarRelacionamentos)
			throws Exception {

		final List<DefinicaoTabela> result = new ArrayList<>();

		try {
			conectar();

			final AtomicReference<DefinicaoEstruturaDados> dbd = new AtomicReference<>(null);
			final AtomicReference<String> esquema = new AtomicReference<>(null);
			final AtomicReference<String> tabela = new AtomicReference<>(null);
			final AtomicReference<String> coluna = new AtomicReference<>(null);
			final AtomicReference<String> esquemaAnt = new AtomicReference<>(null);
			final AtomicReference<String> tabelaAnt = new AtomicReference<>(null);

			// variáveis temporárias
			final AtomicReference<String[]> filtroTmp = new AtomicReference<>();
			final AtomicReference<String> filtroEsquema = new AtomicReference<>(),
					filtroTabela = new AtomicReference<>(), filtroColuna = new AtomicReference<>();

			final DatabaseMetaData meta = this.connection.getMetaData();

			final AtomicReference<List<DefinicaoEstruturaDados>> lote = new AtomicReference<>(new ArrayList<>());

			// Functional Interfaces
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
							throw new RuntimeException("Problemas de manipulação do Banco de Dados", e);
						}
					}
					// limpar o lote
					lote.set(new ArrayList<>());
				}
			};
			final Consumer<String> filtroAnalise = (filtro) -> {
				filtroTmp.set(filtro.split("\\."));
				filtroEsquema.set(filtroTmp.get().length >= 1 && filtroTmp.get()[0].trim().length() >= 1
						? filtroTmp.get()[0].trim()
						: "");
				filtroTabela.set(filtroTmp.get().length >= 2 && filtroTmp.get()[1].trim().length() >= 1
						? filtroTmp.get()[1].trim()
						: "");
				filtroColuna.set(filtroTmp.get().length >= 3 && filtroTmp.get()[2].trim().length() >= 1
						? filtroTmp.get()[2].trim()
						: "");
			};

			// buscar pelos filtros informados
			for (String filtroAdiciona : filtroSet) {

				// carregar os meta dados baseados no filtro informado
				filtroAnalise.accept(filtroAdiciona);
				filtroColuna.set(filtroColuna.get() == null ? "%" : filtroColuna.get());
				ResultSet metaReg = null;
				switch (this.informacaoConexao.getPlataformaBanco()) {
				default:
				case MYSQL:
					metaReg = meta.getColumns(filtroEsquema.get(), null, filtroTabela.get(), filtroColuna.get());
					break;
				case POSTGRES:
					metaReg = meta.getColumns(null, filtroEsquema.get(), filtroTabela.get(), filtroColuna.get());
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
					principal: while (metaReg.next()) {

						esquema.set(metaReg.getString(1) == null ? metaReg.getString(2) : metaReg.getString(1));
						tabela.set(metaReg.getString(3));
						coluna.set(metaReg.getString(4));

						// ignorar views?
						if (this.informacaoConexao.isSomenteTabelas()) {
							PreparedStatement psView = null;
							ResultSet rsView = null;

							switch (this.informacaoConexao.getPlataformaBanco()) {
							default:
							case MYSQL:
								throw new IllegalArgumentException("falta implementar");
							// break;
							case POSTGRES:
								StringBuilder sql = new StringBuilder();
								sql.append(
										"SELECT 1 FROM INFORMATION_SCHEMA.views WHERE table_schema = ? AND table_name = ?")
										.append("\n");
								sql.append("union").append("\n");
								sql.append(
										"SELECT 1 FROM pg_catalog.pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relkind = 'm' AND n.nspname = ? AND c.relname = ?")
										.append("\n");
								psView = this.connection.prepareStatement(sql.toString());
								psView.setString(1, esquema.get());
								psView.setString(2, tabela.get());
								psView.setString(3, esquema.get());
								psView.setString(4, tabela.get());
								rsView = psView.executeQuery();
								break;
							}
							if (rsView != null && rsView.next()) {
								continue;
							}
						}

						// verificar se o registro deve ser IGNORADO
						for (String filtroExclui : excluiFiltroSet) {
							filtroAnalise.accept(filtroExclui);

							// ajustar o caractere coringa para fazer avaliação por regex
							if (filtroEsquema.get() != null) {
								filtroEsquema.set(filtroEsquema.get().replaceAll("%", "(\\\\w)*"));
							}
							if (filtroTabela.get() != null) {
								filtroTabela.set(filtroTabela.get().replaceAll("%", "(\\\\w)*"));
							}
							if (filtroColuna.get() != null) {
								filtroColuna.set(filtroColuna.get().replaceAll("%", "(\\\\w)*"));
							}

							// filtrar por coluna
							if (filtroTmp.get().length >= 3) {
								if (esquema.get().matches(filtroEsquema.get())
										&& tabela.get().matches(filtroTabela.get())
										&& coluna.get().matches(filtroColuna.get())) {
									continue principal;
								}
							}
							// filtrar por tabela
							else if (filtroTmp.get().length == 2) {
								if (esquema.get().matches(filtroEsquema.get())
										&& tabela.get().matches(filtroTabela.get())) {
									continue principal;
								}
							}
							// filtrar por esquema
							else if (filtroTmp.get().length == 1) {
								if (esquema.get().matches(filtroEsquema.get())) {
									continue principal;
								}
							}
						}

						// configuração inicial de coleta de dados
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
		} finally {
			desconectar();
		}

		return result;
	}

	private synchronized void desconectar() {
		if (this.connection != null) {
			try {
				this.connection.close();
				this.connection = null;
			} catch (SQLException e) {
				throw new RuntimeException("Problemas ao tentar conexão ao Banco de Dados", e);
			}
		}
	}

	// para testes futuros se necessário
	private void printAllSequenceName(Connection conn) throws JDBCConnectionException, SQLException {
		DialectResolver dialectResolver = new StandardDialectResolver();
		Dialect dialect = dialectResolver
				.resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(conn.getMetaData()));

		if (dialect.supportsSequences()) {
			String sql = dialect.getQuerySequencesString();
			if (sql != null) {
				try (Statement statement = conn.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
					while (rs.next()) {
						ResultSetMetaData md = rs.getMetaData();
						StringBuffer sb = new StringBuffer();
						for (int i = 1; i <= md.getColumnCount(); i++) {
							sb.append(rs.getString(i)).append(", ");
						}
						System.out.println("Sequence Name : " + sb.toString());
					}
				}
			}
		}
	}

}
