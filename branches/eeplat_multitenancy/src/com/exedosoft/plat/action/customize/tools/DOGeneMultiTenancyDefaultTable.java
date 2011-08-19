package com.exedosoft.plat.action.customize.tools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.exedosoft.plat.ExedoException;
import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOBO;
import com.exedosoft.plat.bo.DODataSource;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.gene.jquery.SqlCol;
import com.exedosoft.plat.util.DOGlobals;
import com.exedosoft.plat.util.StringUtil;

public class DOGeneMultiTenancyDefaultTable extends DOAbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 558266835833057020L;

	private static Log log = LogFactory
			.getLog(DOGeneMultiTenancyDefaultTable.class);
	
	private static DODataSource dss = DODataSource.getDataSourceByID("mysqlexample");
 	private static String tenantFileBase = DOGlobals.getValue("tenant_db");

	@Override
	public String excute() throws ExedoException {

		DOBO aBO = DOBO.getDOBOByName("multi_tenancy");
		
		if(aBO!=null){
			BOInstance curTenant = aBO.getCorrInstance();
			File aFile = new File( tenantFileBase + curTenant.getValue("name") );
			if(aFile.exists()){
				this.setEchoValue("该租户已经创建!");
				return this.NO_FORWARD;
			}
			if(curTenant!=null){
				
				String multi_datasource_uid = curTenant.getValue("multi_datasource_uid");
				if(multi_datasource_uid!=null){
					DOService findDataSource = DOService
					.getService("multi_datasource_browse");
					BOInstance aBI = findDataSource.getInstance(multi_datasource_uid);
					if(aBI!=null){
						dss = (DODataSource)aBI.toObject(DODataSource.class);
						createTenant(curTenant.getValue("name"));
					}
				}
			}
		}
		this.setEchoValue("初始化成功！");
		return DEFAULT_FORWARD;

	}
	
	private Collection getCols(String aTable) {

		Collection cc = new ArrayList();
		Connection con = null;
		try {
			con = dss.getConnection();
			DatabaseMetaData meta = con.getMetaData();
			System.out.println(con.getCatalog());
			ResultSet rs = meta.getColumns(null, null, aTable, null);
			while (rs.next()) {
				SqlCol qc = new SqlCol();
				qc.setName(rs.getString("COLUMN_NAME").toLowerCase());
				qc.setDataType(rs.getInt("DATA_TYPE"));
				qc.setScale(rs.getInt("DECIMAL_DIGITS"));
				qc.setNullable(rs.getString("IS_NULLABLE"));
				qc.setSize(rs.getInt("COLUMN_SIZE"));
				cc.add(qc);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (!con.isClosed()) {
					con.close();
				}
			} catch (SQLException ex1) {
				ex1.printStackTrace();
			}

		}
		return cc;
	}


	public boolean createMultiTenancyTable(String tenant, String aTableName) {


		Collection<SqlCol> cc = getCols(aTableName);
		StringBuffer buffer = new StringBuffer("create view ").append(tenant)
				.append("_").append(aTableName).append("  as select ");

		for (Iterator<SqlCol> it = cc.iterator(); it.hasNext();) {
			SqlCol sc = it.next();
			/**
			 * 表do_org_user毕竟特殊，需要知道其所属的租户ID
			 */
			if (!aTableName.equals("do_org_user")
					&& sc.getName().equalsIgnoreCase("tenancyId")) {
				continue;
			}
			buffer.append(sc.getName());
			buffer.append(",");
		}
		/**
		 * 删除多余的","
		 */
		buffer.deleteCharAt(buffer.length() - 1);

		buffer.append(" from ").append(aTableName).append(" where tenancyId='")
				.append(tenant).append("'");

		log.info("create view sql::" + buffer);

		Connection con = dss.getConnection();
		try {
			PreparedStatement pstmt = con.prepareStatement(buffer.toString());
			pstmt.executeUpdate();
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean createMultiTennacyTableOfWf(String tenant) {

		StringBuffer wfBj = new StringBuffer(
				"CREATE VIEW #TENANT#_wf_bj AS select distinct wpi.WFI_Desc AS wfi_desc,wpi.startUser AS")
				.append(
						" startuser,wpi.startTime AS starttime,ni.OBJUID AS contextNiUid,wpi.OBJUID AS ")

				.append(
						" contextPIUid,wpi.curState AS curState,wpi.instance_uid AS instance_uid,ni.performerUid AS ")

				.append(
						" USER_UID,ni.nodeDate AS nodeDate from (do_wfi_his_nodeinstance ni join  ")

				.append(
						" do_wfi_his_processinstance wpi on((ni.PI_UID = wpi.OBJUID))) where ((ni.ExeStatus = 3) and  ")

				.append(
						" (wpi.ExeStatus = 3)   and ni.tenancyid='#TENANT#' and wpi.tenancyid='#TENANT#') ");
		
		

		StringBuffer wfDb = new StringBuffer(
				" CREATE VIEW #TENANT#_wf_db AS select distinct wpi.curState AS curstate,ni.node_uid AS   ")

				.append(
						" node_uid,ni.nodeDate AS nodeDate,ni.OBJUID AS contextNIUid,wpi.OBJUID AS   ")

				.append(
						" contextPIUid,wpi.instance_uid AS instance_uid,ni.pass_txt AS pass_txt,ni.reject_txt AS   ")

				.append(
						" reject_txt,ur.USER_UID AS user_uid,wpi.WFI_Desc AS WFI_Desc,wpi.startUser AS   ")

				.append(
						" startUser,wpi.startTime AS startTime from (((do_wfi_nodeinstance ni join   ")

				.append(
						" do_wfi_processinstance wpi) join do_org_user_role ur) join do_authorization a) where   ")

				.append(
						" ((wpi.OBJUID = ni.PI_UID) and (a.parterUid = _utf8'9') and (a.ouUid = ur.ROLE_UID) and   ")

				.append(
						" (ni.node_uid = a.whatUid) and (ni.ExeStatus = 2) and (wpi.ExeStatus = 2)  and   ")

				.append(
						" ni.tenancyid='#TENANT#' and wpi.tenancyid='#TENANT#'  and a.tenancyid='#TENANT#')   ");

		StringBuffer wfYb = new StringBuffer(
				" CREATE VIEW #TENANT#_wf_yb AS select distinct wpi.WFI_Desc AS wfi_desc,wpi.startUser AS ")

				.append(
						" startuser,wpi.startTime AS starttime,ni.OBJUID AS contextNIUid,wpi.OBJUID AS   ")

				.append(
						" contextPIUid,wpi.curState AS curState,wpi.instance_uid AS instance_uid,ni.performerUid AS   ")

				.append(
						" USER_UID,ni.nodeDate AS nodeDate from (do_wfi_nodeinstance ni join do_wfi_processinstance   ")

				.append(
						" wpi on((ni.PI_UID = wpi.OBJUID))) where ((ni.ExeStatus = 3) and (wpi.ExeStatus = 2)  ")
				.append(
						" and ni.tenancyid='#TENANT#' and wpi.tenancyid='#TENANT#')  ");
		
		Connection con = dss.getConnection();
		try {
			Statement pstmt = con.createStatement();
			pstmt.execute(wfBj.toString().replace("#TENANT#", tenant));
			pstmt.execute(wfDb.toString().replace("#TENANT#", tenant));
			pstmt.execute(wfYb.toString().replace("#TENANT#", tenant));
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public static void createTenant(String tenent){
		
		copyFiles(tenent);

		DOGeneMultiTenancyDefaultTable gtd = new DOGeneMultiTenancyDefaultTable();

		gtd.createMultiTenancyTable(tenent, "do_authorization");
		gtd.createMultiTenancyTable(tenent, "do_code_maxsequence");
		gtd.createMultiTenancyTable(tenent, "do_log");
		gtd.createMultiTenancyTable(tenent, "do_log_data");
		gtd.createMultiTenancyTable(tenent, "do_org_dept");
		gtd.createMultiTenancyTable(tenent, "do_org_role");
		gtd.createMultiTenancyTable(tenent, "do_org_user");
		gtd.createMultiTenancyTable(tenent, "do_org_user_delegate");
		gtd.createMultiTenancyTable(tenent, "do_org_user_role");
		gtd.createMultiTenancyTable(tenent, "do_wfi_his_ni_dependency");
		gtd.createMultiTenancyTable(tenent, "do_wfi_his_nodeinstance");
		gtd.createMultiTenancyTable(tenent, "do_wfi_his_processinstance");
		gtd.createMultiTenancyTable(tenent, "do_wfi_his_varinstance");
		gtd.createMultiTenancyTable(tenent, "do_wfi_ni_dependency");
		gtd.createMultiTenancyTable(tenent, "do_wfi_nodeinstance");
		gtd.createMultiTenancyTable(tenent, "do_wfi_processinstance");
		gtd.createMultiTenancyTable(tenent, "do_wfi_varinstance");

		gtd.createMultiTennacyTableOfWf(tenent);
	}
	
	private static void copyFiles(String tenant){
		try {
			StringUtil.copyDirectiory(tenantFileBase + tenant,tenantFileBase + "base");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		DOGeneMultiTenancyDefaultTable.createTenant("tst");
		
		

	}

}