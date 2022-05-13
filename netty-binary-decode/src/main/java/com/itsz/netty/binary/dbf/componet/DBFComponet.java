package com.itsz.netty.binary.dbf.componet;

import com.itsz.netty.binary.IPacket;
import com.itsz.netty.binary.dbf.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DBFComponet {

	private static Logger logger = LoggerFactory.getLogger(DBFComponet.class);
	private static Map<String, Integer> posMapping = new ConcurrentHashMap<String, Integer>();
	private static DBFWriter writer = null;

	private volatile static AtomicInteger maxIndex;

	private String filePath;

	private String charset;

	@PostConstruct
	void init() {
		File file = new File(filePath);
		FileInputStream inputStream = null;
		DBFReader reader = null;
		try {
			inputStream = new FileInputStream(file);
			reader = new DBFReader(inputStream);
			maxIndex = new AtomicInteger(reader.getRecordCount());
			for (int i = 0; i < maxIndex.intValue(); i++) {
				DBFRow row = reader.nextRow();
				if (row == null) {
					break;
				}
				String stockCode = row.getString(0);
				posMapping.put(stockCode, i);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			DBFUtils.close(inputStream);
			DBFUtils.close(reader);
		}
		writer = new DBFWriter(file, Charset.forName(charset));
	}

	/**
	 * 
	 * @param packet
	 */
	public static void writeDBF(IPacket packet) {
		String securityId = packet.getSecurityId();
		Integer index = posMapping.get(securityId);
		if (index == null) {
			index = maxIndex.getAndIncrement();
			posMapping.put(securityId, index);
		}
		try {
			writer.updateOneRecord(generateFirstRow(), 0);
			Object[] data = packet.convert2DBFRow();
			writer.updateOneRecord(data, index);
		} catch (DBFException e) {
			logger.error("写入DBF出错了" + e.getMessage());
		}
	}

	/**
	 * 写入DBF
	 *
	 * @param snapshotList
	 * @throws DBFException
	 */
	public static void writeSJSHQDBF(List<IPacket> snapshotList) throws DBFException {
		try {
			writer.updateOneRecord(generateFirstRow(), 0);
			// 写入数据
			for (IPacket packet : snapshotList) {
				writeDBF(packet);
			}
		} catch (Exception e) {
			logger.info("exception !!!!!!! {}", e);
			;
			throw new DBFException(e.getMessage());
		} finally {
		}

	}

	private static Object[] generateFirstRow() {
		Object[] fristRow = new Object[36];
		for (int i = 0; i < fristRow.length; i++) {
			fristRow[i] = 0;
		}
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);

		fristRow[0] = "000000";
		fristRow[1] = "" + year + (month<10?"0"+month:month) + (day<10?"0"+day:day);
		fristRow[7] = Integer.parseInt("" + (hour<10?"0"+hour:hour) + (minutes<10?"0"+minutes:minutes) + (seconds<10?"0"+seconds:seconds));
		return fristRow;
	}

	public static DBFField[] createSZDBFHeader() {
		DBFField[] fields = new DBFField[36];
		// 证券代码
		fields[0] = new DBFField();
		fields[0].setName("HQZQDM");
		fields[0].setType(DBFDataType.CHARACTER);
		fields[0].setLength(6);

		// 证券简称
		fields[1] = new DBFField();
		fields[1].setName("HQZQJC");
		fields[1].setType(DBFDataType.CHARACTER);
		fields[1].setLength(8);

		// 昨日收盘价
		fields[2] = new DBFField();
		fields[2].setName("HQZRSP");
		fields[2].setType(DBFDataType.NUMERIC);
		fields[2].setLength(9);
		fields[2].setDecimalCount(3);

		// 今日开盘价
		fields[3] = new DBFField();
		fields[3].setName("HQJRKP");
		fields[3].setType(DBFDataType.NUMERIC);
		fields[3].setLength(9);
		fields[3].setDecimalCount(3);

		// 最近成交价
		fields[4] = new DBFField();
		fields[4].setName("HQZJCJ");
		fields[4].setType(DBFDataType.NUMERIC);
		fields[4].setLength(9);
		fields[4].setDecimalCount(3);

		// 成交数量
		fields[5] = new DBFField();
		fields[5].setName("HQCJSL");
		fields[5].setType(DBFDataType.NUMERIC);
		fields[5].setLength(12);
		fields[5].setDecimalCount(0);

		// 成交金额
		fields[6] = new DBFField();
		fields[6].setName("HQCJJE");
		fields[6].setType(DBFDataType.NUMERIC);
		fields[6].setLength(17);
		fields[6].setDecimalCount(3);
		// 成交笔数
		fields[7] = new DBFField();
		fields[7].setName("HQCJBS");
		fields[7].setType(DBFDataType.NUMERIC);
		fields[7].setLength(9);
		fields[7].setDecimalCount(0);

		// 最高成交价
		fields[8] = new DBFField();
		fields[8].setName("HQZGCJ");
		fields[8].setType(DBFDataType.NUMERIC);
		fields[8].setLength(9);
		fields[8].setDecimalCount(3);

		// 最低成交价
		fields[9] = new DBFField();
		fields[9].setName("HQZDCJ");
		fields[9].setType(DBFDataType.NUMERIC);
		fields[9].setLength(9);
		fields[9].setDecimalCount(3);

		// 市盈率 1
		fields[10] = new DBFField();
		fields[10].setName("HQSYL1");
		fields[10].setType(DBFDataType.NUMERIC);
		fields[10].setLength(7);
		fields[10].setDecimalCount(2);
		// 市盈率 2
		fields[11] = new DBFField();
		fields[11].setName("HQSYL2");
		fields[11].setType(DBFDataType.NUMERIC);
		fields[11].setLength(7);
		fields[11].setDecimalCount(2);

		// 价格升跌1
		fields[12] = new DBFField();
		fields[12].setName("HQJSD1");
		fields[12].setType(DBFDataType.NUMERIC);
		fields[12].setLength(9);
		fields[12].setDecimalCount(3);

		// 价格升跌2
		fields[13] = new DBFField();
		fields[13].setName("HQJSD2");
		fields[13].setType(DBFDataType.NUMERIC);
		fields[13].setLength(9);
		fields[13].setDecimalCount(3);

		// 合约持仓量
		fields[14] = new DBFField();
		fields[14].setName("HQHYCC");
		fields[14].setType(DBFDataType.NUMERIC);
		fields[14].setLength(12);
		fields[14].setDecimalCount(0);

		// 卖价位五
		fields[15] = new DBFField();
		fields[15].setName("HQSJW5");
		fields[15].setType(DBFDataType.NUMERIC);
		fields[15].setLength(9);
		fields[15].setDecimalCount(3);

		// 卖数量五
		fields[16] = new DBFField();
		fields[16].setName("HQSSL5");
		fields[16].setType(DBFDataType.NUMERIC);
		fields[16].setLength(12);
		fields[16].setDecimalCount(0);

		// 卖价位四
		fields[17] = new DBFField();
		fields[17].setName("HQSJW4");
		fields[17].setType(DBFDataType.NUMERIC);
		fields[17].setLength(9);
		fields[17].setDecimalCount(3);

		// 卖数量四
		fields[18] = new DBFField();
		fields[18].setName("HQSSL4");
		fields[18].setType(DBFDataType.NUMERIC);
		fields[18].setLength(12);
		fields[18].setDecimalCount(0);

		// 卖价位三
		fields[19] = new DBFField();
		fields[19].setName("HQSJW3");
		fields[19].setType(DBFDataType.NUMERIC);
		fields[19].setLength(9);
		fields[19].setDecimalCount(3);

		// 卖数量三
		fields[20] = new DBFField();
		fields[20].setName("HQSSL3");
		fields[20].setType(DBFDataType.NUMERIC);
		fields[20].setLength(12);
		fields[20].setDecimalCount(0);

		// 卖价位二
		fields[21] = new DBFField();
		fields[21].setName("HQSJW2");
		fields[21].setType(DBFDataType.NUMERIC);
		fields[21].setLength(9);
		fields[21].setDecimalCount(3);

		// 卖数量二
		fields[22] = new DBFField();
		fields[22].setName("HQSSL2");
		fields[22].setType(DBFDataType.NUMERIC);
		fields[22].setLength(12);
		fields[22].setDecimalCount(0);

		// 卖价位一/叫卖揭示价
		fields[23] = new DBFField();
		fields[23].setName("HQSJW1");
		fields[23].setType(DBFDataType.NUMERIC);
		fields[23].setLength(9);
		fields[23].setDecimalCount(3);

		// 卖数量一
		fields[24] = new DBFField();
		fields[24].setName("HQSSL1");
		fields[24].setType(DBFDataType.NUMERIC);
		fields[24].setLength(12);
		fields[24].setDecimalCount(0);

		// 买价位一/叫买揭示价
		fields[25] = new DBFField();
		fields[25].setName("HQBJW1");
		fields[25].setType(DBFDataType.NUMERIC);
		fields[25].setLength(9);
		fields[25].setDecimalCount(3);

		// 买数量一
		fields[26] = new DBFField();
		fields[26].setName("HQBSL1");
		fields[26].setType(DBFDataType.NUMERIC);
		fields[26].setLength(12);
		fields[26].setDecimalCount(0);

		// 买价位二
		fields[27] = new DBFField();
		fields[27].setName("HQBJW2");
		fields[27].setType(DBFDataType.NUMERIC);
		fields[27].setLength(9);
		fields[27].setDecimalCount(3);

		// 买数量二
		fields[28] = new DBFField();
		fields[28].setName("HQBSL2");
		fields[28].setType(DBFDataType.NUMERIC);
		fields[28].setLength(12);
		fields[28].setDecimalCount(0);

		// 买价位三
		fields[29] = new DBFField();
		fields[29].setName("HQBJW3");
		fields[29].setType(DBFDataType.NUMERIC);
		fields[29].setLength(9);
		fields[29].setDecimalCount(3);

		// 买数量三
		fields[30] = new DBFField();
		fields[30].setName("HQBSL3");
		fields[30].setType(DBFDataType.NUMERIC);
		fields[30].setLength(12);
		fields[30].setDecimalCount(0);

		// 买价位四
		fields[31] = new DBFField();
		fields[31].setName("HQBJW4");
		fields[31].setType(DBFDataType.NUMERIC);
		fields[31].setLength(9);
		fields[31].setDecimalCount(3);

		// 买数量四
		fields[32] = new DBFField();
		fields[32].setName("HQBSL4");
		fields[32].setType(DBFDataType.NUMERIC);
		fields[32].setLength(12);
		fields[32].setDecimalCount(0);

		// 买价位五
		fields[33] = new DBFField();
		fields[33].setName("HQBJW5");
		fields[33].setType(DBFDataType.NUMERIC);
		fields[33].setLength(9);
		fields[33].setDecimalCount(3);

		// 买数量五
		fields[34] = new DBFField();
		fields[34].setName("HQBSL5");
		fields[34].setType(DBFDataType.NUMERIC);
		fields[34].setLength(12);
		fields[34].setDecimalCount(0);
		// 交易时间
		fields[35] = new DBFField();
		fields[35].setName("TRADETIME");
		fields[35].setType(DBFDataType.NUMERIC);
		fields[35].setLength(12);
		fields[35].setDecimalCount(0);

		return fields;
	}

}
