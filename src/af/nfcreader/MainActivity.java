package af.nfcreader;

import java.io.IOException;
import java.util.Arrays;

import af.nfcreader.Iso7816.Response;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

    NfcAdapter nfcAdapter;  
    TextView promt;  
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); 
        promt = (TextView) findViewById(R.id.promt);  
        // 获取默认的NFC控制器  
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);  
        if (nfcAdapter == null) {  
            promt.setText("设备不支持NFC！");  
//            finish();
            return;  
        }  
        if (!nfcAdapter.isEnabled()) {  
            promt.setText("请在系统设置中先启用NFC功能！");  
//            finish();  
            return;  
        }  
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override  
    protected void onResume() {  
        super.onResume();  
        //得到是否检测到ACTION_TECH_DISCOVERED触发  
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())
        		||NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())
        		||NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())
        		) {  
            //处理该intent  
            processIntent(getIntent());  
        }  
    }  
    //字符序列转换为16进制字符串  
    private String bytesToHexString(byte[] src) {  
        StringBuilder stringBuilder = new StringBuilder("0x");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        char[] buffer = new char[2];  
        for (int i = 0; i < src.length; i++) {  
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);  
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);  
            System.out.println(buffer);  
            stringBuilder.append(buffer);  
        }  
        return stringBuilder.toString();  
    }  
  
    /** 
     * Parses the NDEF Message from the intent and prints to the TextView 
     */  
    private void processIntent(Intent intent) {  
        //取出封装在intent中的TAG  
    	Log.wtf("zzz", intent.getAction());
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);  
        for (String tech : tagFromIntent.getTechList()) {  
            System.out.println(tech);  
        	Log.wtf("zzz", tech);
        }  
        boolean auth = false;  
        //读取TAG  
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        if(mfc != null){
        	try {  
                String metaInfo = "";  
                //Enable I/O operations to the tag from this TagTechnology object.  
                mfc.connect();  
                int type = mfc.getType();//获取TAG的类型  
                int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数  
                String typeS = "";  
                switch (type) {  
                case MifareClassic.TYPE_CLASSIC:  
                    typeS = "TYPE_CLASSIC";  
                    break;  
                case MifareClassic.TYPE_PLUS:  
                    typeS = "TYPE_PLUS";  
                    break;  
                case MifareClassic.TYPE_PRO:  
                    typeS = "TYPE_PRO";  
                    break;  
                case MifareClassic.TYPE_UNKNOWN:  
                    typeS = "TYPE_UNKNOWN";  
                    break;  
                }  
                metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"  
                        + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";  
                for (int j = 0; j < sectorCount; j++) {  
                    //Authenticate a sector with key A.  
                    auth = mfc.authenticateSectorWithKeyA(j,  
                            MifareClassic.KEY_DEFAULT);  
                    int bCount;  
                    int bIndex;  
                    if (auth) {  
                        metaInfo += "Sector " + j + ":验证成功\n";  
                        // 读取扇区中的块  
                        bCount = mfc.getBlockCountInSector(j);  
                        bIndex = mfc.sectorToBlock(j);  
                        for (int i = 0; i < bCount; i++) {  
                            byte[] data = mfc.readBlock(bIndex);  
                            metaInfo += "Block " + bIndex + " : "  
                                    + bytesToHexString(data) + "\n";  
                            bIndex++;  
                        }  
                    } else {  
                        metaInfo += "Sector " + j + ":验证失败\n";  
                    }  
                }  
                promt.setText(metaInfo);
                return;
            } catch (Exception e) {  
                e.printStackTrace();  
                return;
            }
        }
        
        final IsoDep isodep = IsoDep.get(tagFromIntent);
		if (isodep != null){
//			StandardPboc.readCard(isodep, tagFromIntent);
			byte[] id = isodep.getTag().getId();
			nfcTag = isodep;
			try {
				isodep.connect();
				;
				Log.wtf("zzz", String.valueOf(isodep.getHistoricalBytes()));
//				Log.wtf("zzz", String.valueOf(isodep.getHiLayerResponse()));
//				Log.wtf("zzz", String.valueOf(isodep.getTag()));
				

				Response info = readBinary(SFI_EXTRA);
				Log.wtf("zzz", String.valueOf(info.getBytes()));
				Log.wtf("zzz", String.valueOf(info.getSw1()));
				Log.wtf("zzz", String.valueOf(info.getSw12String()));
				Log.wtf("zzz", String.valueOf(info.getSw2()));
				
				isodep.close();
				
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		
		
		
		final NfcF nfcf = NfcF.get(tagFromIntent);
		if (nfcf != null){
//			FeliCa.Tag tag = new FeliCa.Tag(nfcf);
			NfcF nfcTag = nfcf;
			try {
				nfcTag.connect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
        
        
        
    }  
    
	static class Response extends Iso7816 {
		public static final byte[] EMPTY = {};
		public static final byte[] ERROR = { 0x6F, 0x00 }; // SW_UNKNOWN

		public Response(byte[] bytes) {
			super((bytes == null || bytes.length < 2) ? Response.ERROR : bytes);
		}

		public byte getSw1() {
			return data[data.length - 2];
		}

		public byte getSw2() {
			return data[data.length - 1];
		}

		public String getSw12String() {
			int sw1 = getSw1() & 0x000000FF;
			int sw2 = getSw2() & 0x000000FF;
			return String.format("0x%02X%02X", sw1, sw2);
		}

		public short getSw12() {
			final byte[] d = this.data;
			int n = d.length;
			return (short) ((d[n - 2] << 8) | (0xFF & d[n - 1]));
		}

		public boolean isOkey() {
			return equalsSw12(SW_NO_ERROR);
		}

		public boolean equalsSw12(short val) {
			return getSw12() == val;
		}

		@Override
		public int size() {
			return data.length - 2;
		}

		@Override
		public byte[] getBytes() {
			return isOkey() ? Arrays.copyOfRange(data, 0, size())
					: Response.EMPTY;
		}
	}
	public Response readBinary(int sfi) throws IOException {
		final byte[] cmd = { (byte) 0x00, // CLA Class
				(byte) 0xB0, // INS Instruction
				(byte) (0x00000080 | (sfi & 0x1F)), // P1 Parameter 1
				(byte) 0x00, // P2 Parameter 2
				(byte) 0x00, // Le
		};

		return new Response(transceive(cmd));
	}
	public byte[] transceive(final byte[] cmd) throws IOException {
		try {
			byte[] rsp = null;

			byte c[] = cmd;
			do {
				byte[] r = nfcTag.transceive(c);
				if (r == null)
					break;

				int N = r.length - 2;
				if (N < 0) {
					rsp = r;
					break;
				}

				if (r[N] == CH_STA_LE) {
					c[c.length - 1] = r[N + 1];
					continue;
				}

				if (rsp == null) {
					rsp = r;
				} else {
					int n = rsp.length;
					N += n;

					rsp = Arrays.copyOf(rsp, N);

					n -= 2;
					for (byte i : r)
						rsp[n++] = i;
				}

				if (r[N] != CH_STA_MORE)
					break;

				byte s = r[N + 1];
				if (s != 0) {
					c = CMD_GETRESPONSE.clone();
				} else {
					rsp[rsp.length - 1] = CH_STA_OK;
					break;
				}

			} while (true);

			return rsp;

		} catch (Exception e) {
			return Response.ERROR;
		}
	}
	


	protected enum HINT {
		STOP, GONEXT, RESETANDGONEXT,
	}

	IsoDep nfcTag;
	protected final static byte[] DFI_MF = { (byte) 0x3F, (byte) 0x00 };
	protected final static byte[] DFI_EP = { (byte) 0x10, (byte) 0x01 };

	protected final static byte[] DFN_PSE = { (byte) '1', (byte) 'P', (byte) 'A', (byte) 'Y',
			(byte) '.', (byte) 'S', (byte) 'Y', (byte) 'S', (byte) '.', (byte) 'D', (byte) 'D',
			(byte) 'F', (byte) '0', (byte) '1', };

	protected final static byte[] DFN_PXX = { (byte) 'P' };

	protected final static int SFI_EXTRA = 21;

	protected static int MAX_LOG = 10;
	protected static int SFI_LOG = 24;

	protected final static byte TRANS_CSU = 6;
	protected final static byte TRANS_CSU_CPX = 9;
	private final static int SFI_EXTRA_LOG = 4;
	private final static int SFI_EXTRA_CNT = 5;
	private static final byte CH_STA_OK = (byte) 0x90;
	private static final byte CH_STA_MORE = (byte) 0x61;
	private static final byte CH_STA_LE = (byte) 0x6C;
	private static final byte CMD_GETRESPONSE[] = { 0, (byte) 0xC0, 0, 0,
			0, };
}
