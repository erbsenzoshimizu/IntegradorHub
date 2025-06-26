package br.com.erbs.integradorhub.principal;

import java.io.File;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Cancelamento {

    public static void gerarXmlCancelamento(String tpAmb, String cnpj, String chaveNFe, String nProt, String justificativa, String arquivoSaida)
            throws Exception {

        // Valida chave
        if (chaveNFe == null || chaveNFe.length() != 44) {
            throw new IllegalArgumentException("Chave NFC-e deve ter 44 dígitos, foi passada: " + chaveNFe);
        }

        String cOrgao = extrairCodigoOrgao(chaveNFe);
        String idEvento = "ID110111" + chaveNFe + "01";
        if (!idEvento.matches("ID\\d{52}")) {
            throw new IllegalStateException("IdEvento inválido: " + idEvento);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        String dhEvento = ZonedDateTime.now().format(formatter);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Cria envEvento com namespace NFC-e
        Element envento = doc.createElementNS("http://www.portalfiscal.inf.br/nfe", "evento");
        envento.setAttribute("versao", "1.00");
        doc.appendChild(envento);

        // infEvento
        Element infEvento = doc.createElement("infEvento");
        infEvento.setAttribute("Id", idEvento);
        envento.appendChild(infEvento);

        addTag(doc, infEvento, "cOrgao", cOrgao);
        addTag(doc, infEvento, "tpAmb", tpAmb);
        addTag(doc, infEvento, "CNPJ", cnpj);
        addTag(doc, infEvento, "chNFe", chaveNFe);
        addTag(doc, infEvento, "dhEvento", dhEvento);
        addTag(doc, infEvento, "tpEvento", "110111");
        addTag(doc, infEvento, "nSeqEvento", "1");
        addTag(doc, infEvento, "verEvento", "1.00");

        // detEvento
        Element detEvento = doc.createElement("detEvento");
        detEvento.setAttribute("versao", "1.00");
        infEvento.appendChild(detEvento);

        addTag(doc, detEvento, "descEvento", "Cancelamento");
        addTag(doc, detEvento, "nProt", nProt);
        addTag(doc, detEvento, "xJust", justificativa);

        // Assinatura fake para testes
        /*adicionarSignatureFake(doc, evento, idEvento);*/

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        arquivoSaida += "Can" + chaveNFe + ".xml";
        File outFile = new File(arquivoSaida);
        transformer.transform(new DOMSource(doc), new StreamResult(outFile));
    }

    private static void addTag(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private static String extrairCodigoOrgao(String chaveNFe) {
        return chaveNFe.substring(0, 2);
    }

    private static void adicionarSignatureFake(Document doc, Element evento, String idEvento) {
        final String ds = "http://www.w3.org/2000/09/xmldsig#";
        Element sig = doc.createElementNS(ds, "ds:Signature");
        sig.setAttribute("xmlns:ds", ds);
        evento.appendChild(sig);

        Element si = doc.createElementNS(ds, "ds:SignedInfo");
        sig.appendChild(si);

        Element cm = doc.createElementNS(ds, "ds:CanonicalizationMethod");
        cm.setAttribute("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
        si.appendChild(cm);

        Element sm = doc.createElementNS(ds, "ds:SignatureMethod");
        sm.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
        si.appendChild(sm);

        Element ref = doc.createElementNS(ds, "ds:Reference");
        ref.setAttribute("URI", "#" + idEvento);
        si.appendChild(ref);

        Element transforms = doc.createElementNS(ds, "ds:Transforms");
        ref.appendChild(transforms);

        Element tr = doc.createElementNS(ds, "ds:Transform");
        tr.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#enveloped-signature");
        transforms.appendChild(tr);

        Element tr2 = doc.createElementNS(ds, "ds:Transform");
        tr2.setAttribute("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
        transforms.appendChild(tr2);

        Element dm = doc.createElementNS(ds, "ds:DigestMethod");
        dm.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#sha1");
        ref.appendChild(dm);

        Element dv = doc.createElementNS(ds, "ds:DigestValue");
        dv.setTextContent("SGVsbG9Xb3JsZA==");
        ref.appendChild(dv);

        Element sv = doc.createElementNS(ds, "ds:SignatureValue");
        sv.setTextContent("SGVsbG9Xb3JsZA==");
        sig.appendChild(sv);

        Element ki = doc.createElementNS(ds, "ds:KeyInfo");
        Element x509 = doc.createElementNS(ds, "ds:X509Data");
        Element cert = doc.createElementNS(ds, "ds:X509Certificate");
        cert.setTextContent("SGVsbG9Xb3JsZA==");
        x509.appendChild(cert);
        ki.appendChild(x509);
        sig.appendChild(ki);
    }

    public static void main(String[] args) {
        try {
            gerarXmlCancelamento(
                    "2",
                    "17645625005091",
                    "53250617645625005091650020000000611664051663",
                    "353250000076900",
                    "Erro de digitação no CPF do consumidor.",
                    "C:/temp/"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}