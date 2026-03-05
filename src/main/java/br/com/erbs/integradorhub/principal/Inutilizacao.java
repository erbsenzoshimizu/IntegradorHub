package br.com.erbs.integradorhub.principal;

import java.io.File;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

public class Inutilizacao {

    /*String tpAmb, String cnpj, String chaveNFe, String nProt, String justificativa, String arquivoSaida*/
    public static void gerarXmlInutilizacao(String tpAmb, String uf, String cnpj, Integer serie, Integer numero, String justificativa, String arquivoSaida)
            throws Exception {

        String cUf = obterCodigoUF(uf);
        String ano = LocalDate.now().format(DateTimeFormatter.ofPattern("yy"));
        String serieFormatada = String.format("%03d", serie);
        String numeroFormatado = String.format("%09d", numero);
        
        String idEvento = "ID" + cUf + ano + cnpj + "65" + serieFormatada + numeroFormatado + numeroFormatado;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Cria envEvento com namespace NFC-e
        Element inutNFe = doc.createElementNS("http://www.portalfiscal.inf.br/nfe", "inutNFe");
        inutNFe.setAttribute("versao", "4.00");
        doc.appendChild(inutNFe);

        // infEvento
        Element infInut = doc.createElement("infInut");
        infInut.setAttribute("Id", idEvento);
        inutNFe.appendChild(infInut);

        addTag(doc, infInut, "tpAmb", tpAmb);
        addTag(doc, infInut, "xServ", "INUTILIZAR");
        addTag(doc, infInut, "cUF", cUf);
        addTag(doc, infInut, "ano", ano);
        addTag(doc, infInut, "CNPJ", cnpj);
        addTag(doc, infInut, "mod", "65");
        addTag(doc, infInut, "serie", String.valueOf(serie));
        addTag(doc, infInut, "nNFIni", String.valueOf(numero));
        addTag(doc, infInut, "nNFFin", String.valueOf(numero));
        addTag(doc, infInut, "xJust", justificativa);

        // Assinatura fake para testes
        /*adicionarSignatureFake(doc, evento, idEvento);*/
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        
        arquivoSaida += "Inu" + cUf + ano + cnpj + "65" + serieFormatada + numeroFormatado + numeroFormatado + "_" + timestamp + ".xml";
        File outFile = new File(arquivoSaida);
        transformer.transform(new DOMSource(doc), new StreamResult(outFile));
    }

    private static void addTag(Document doc, Element parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    public static String obterCodigoUF(String sigla) {
        if (sigla == null) {
            return null;
        }

        switch (sigla.trim().toUpperCase()) {
            case "RO":
                return "11";
            case "AC":
                return "12";
            case "AM":
                return "13";
            case "RR":
                return "14";
            case "PA":
                return "15";
            case "AP":
                return "16";
            case "TO":
                return "17";
            case "MA":
                return "21";
            case "PI":
                return "22";
            case "CE":
                return "23";
            case "RN":
                return "24";
            case "PB":
                return "25";
            case "PE":
                return "26";
            case "AL":
                return "27";
            case "SE":
                return "28";
            case "BA":
                return "29";
            case "MG":
                return "31";
            case "ES":
                return "32";
            case "RJ":
                return "33";
            case "SP":
                return "35";
            case "PR":
                return "41";
            case "SC":
                return "42";
            case "RS":
                return "43";
            case "MS":
                return "50";
            case "MT":
                return "51";
            case "GO":
                return "52";
            case "DF":
                return "53";
            default:
                return null; // UF inválida
        }
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
            gerarXmlInutilizacao(
                    "2", 
                    "MS", 
                    "17645625003552", 
                    1, 
                    2, 
                    "Mensagem Manual - ERRO DE VALORES", 
                    "C:/temp/"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
