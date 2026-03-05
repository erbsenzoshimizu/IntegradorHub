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

    public static void main(String[] args) {
        try {
            gerarXmlCancelamento(
                    "2",
                    "17645625003552",
                    "50250717645625003552650010000000011331052258",
                    "150250000113154",
                    "Erro de digitação no CPF do consumidor.",
                    "C:/temp/"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}