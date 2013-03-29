/** Custom Certificate SSL Trust Manager **/

package name.devnull.ttrss.util;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

public class CustomCertTrustManager implements X509TrustManager {
	private X509TrustManager m_sys_trust=null;
	private X509TrustManager m_local_trust=null;
	private X509Certificate[] m_accepted=null;
	
	private static final String TAG = CustomCertTrustManager.class.getSimpleName();
	
	private static X509TrustManager findTM(TrustManagerFactory tmf){
		TrustManager tms[] = tmf.getTrustManagers();
		for(int i = 0 ; i < tms.length ; i++){
			if (tms[i] instanceof X509TrustManager) {
				return((X509TrustManager) tms[i]);
			}
		}
		return null;
	}
	public CustomCertTrustManager(KeyStore local_ks){
		
		//Get the current system trust manager
		TrustManagerFactory tm;
		
		try{
			tm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        tm.init((KeyStore) null);
	
	        this.m_sys_trust = findTM(tm);
	        if (this.m_sys_trust == null) {
                throw new IllegalStateException(
                        "Couldn't find [system] X509TrustManager");
            }
	        
			tm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        tm.init(local_ks);
	
	        this.m_local_trust = findTM(tm);
	        if (this.m_local_trust == null) {
                throw new IllegalStateException(
                        "Couldn't find [local] X509TrustManager");
            }
		}
		catch (GeneralSecurityException e) {
			Log.e(TAG,"Error finding trust manager");
            throw new RuntimeException(e);
        }
		
		List<X509Certificate> issuerLst = new ArrayList<X509Certificate>();
		for (X509Certificate cert : this.m_sys_trust.getAcceptedIssuers()){
			issuerLst.add(cert);
		}
		for (X509Certificate cert : this.m_local_trust.getAcceptedIssuers()){
			issuerLst.add(cert);
		}
		this.m_accepted=issuerLst.toArray(new X509Certificate[issuerLst.size()]);
	}
	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		try {
            Log.d(TAG, "checkClientTrusted() with system trust...");
            this.m_sys_trust.checkClientTrusted(arg0, arg1);
        } catch (CertificateException ce) {
            Log.d(TAG, "checkClientTrusted() with local trust...");
            this.m_local_trust.checkClientTrusted(arg0, arg1);
        }
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		try {
            Log.d(TAG, "checkServerTrusted() with system trust...");
            this.m_sys_trust.checkServerTrusted(chain, authType);
        } catch (CertificateException ce) {
            Log.d(TAG, "checkServerTrusted() with local trust...");
            this.m_local_trust.checkServerTrusted(chain, authType);
        }
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return this.m_accepted;
	}
	
}