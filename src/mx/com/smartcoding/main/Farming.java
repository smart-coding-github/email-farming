package mx.com.smartcoding.main;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Farming {

	private static String TARGET = "http://www.seccionamarilla.com.mx/categorias/salones-para-fiestas/guanajuato";

	public static List<String> getMunicipios(String url) throws IOException {

		Document doc = Jsoup.connect(url).get();
		Elements municipios = doc.select("#contenido li a");

		List<String> muns = new LinkedList<String>();
		for (int i = 0; i < municipios.size(); i++) {
			muns.add(municipios.get(i).attr("href").replace("/1", "/"));
		}

		return muns;

	}

	public static String getMailInMoreInfo(String url) throws SocketTimeoutException, IOException {

		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		Document doc_b = Jsoup.connect(url).get();
		Elements mails = doc_b.select("a.correo");

		return mails.size() > 0 ? mails.get(0).attr("href").trim().replace("mailto:", "") : null;

	}

	public static void main(String[] args) throws Exception {

		int count = 1;

		List<String> muns = getMunicipios(TARGET);
		Iterator<String> it = muns.iterator();

		while (it.hasNext()) {

			String rootTarget = "http://www.seccionamarilla.com.mx" + it.next();

			for (int k = 1; k <= 50; k++) {

				String target = rootTarget + k;

				// System.out.println(">>> Farming ... " + target);

				Document doc;
				try {
					doc = Jsoup.connect(target).get();
				} catch (Exception e1) {
					k--;
					continue;
				}

				Elements companies = doc.select(".colder").select(".vcard");

				// Si la categoria no tiene resultados seguimos con el siguiente municipio.
				if (companies.size() == 0) {
					break;
				}

				for (int i = 0; i < companies.size(); i++) {

					Elements names = companies.get(i).select("h3.org").select(".ProductosMx");
					Elements masInfo = companies.get(i).select(".mas_info");
					Elements addrs = companies.get(i).select(".street-address");
					Elements tels = companies.get(i).select(".tel");

					String name = names.size() > 0 ? names.get(0).text() : "Sin nombre";
					String addr = addrs.size() > 0 ? addrs.get(0).text() : "Sin dirección";
					String tel = tels.size() > 0 ? tels.get(0).text() : "Sin Telefono";
					String url = masInfo.size() > 0 ? masInfo.get(0).attr("href") : null;

					try {

						String email = null;

						if (url != null && (email = getMailInMoreInfo(url)) != null) {

							System.out.print(email + ",");
							System.out.print(name + ",");
							System.out.print(tel + ",");
							System.out.println(addr);

							count++;

						} else {
							continue;
						}

					} catch (Exception e) {
						// System.out.println(">>> Retry ...");
						i--;
					}

				}
			}

			// System.out.println(">>> Total:" + count);
		}

	}

}
