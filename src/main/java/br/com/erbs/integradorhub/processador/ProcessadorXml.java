package br.com.erbs.integradorhub.processador;

import br.com.erbs.integradorhub.principal.Principal;
import br.com.erbs.integradorhub.webservice.WebServiceSDE;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ProcessadorXml {

    public static void processarXml(String arquivoEntrada, String arquivoSaida) {
        Principal principal = new Principal();

        try {
            String namespace = "http://www.portalfiscal.inf.br/nfe";

            // Carrega o XML
            File xmlFile = new File(arquivoEntrada);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            // Substitui <dhEmi> pela data/hora atuais em Zone America/Sao_Paulo
            NodeList dhEmiList = doc.getElementsByTagNameNS("*", "dhEmi");
            if (dhEmiList.getLength() > 0) {
                Element dhEmi = (Element) dhEmiList.item(0);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                OffsetDateTime agora = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"));
                String isoNow = agora.format(formatter);
                dhEmi.setTextContent(isoNow);
            }

            // Remove a tag <vol>
            NodeList volList = doc.getElementsByTagNameNS("*", "vol");
            if (volList.getLength() > 0) {
                Node vol = volList.item(0);
                vol.getParentNode().removeChild(vol);
            }
            // Remove a tag <dest>
            NodeList destList = doc.getElementsByTagNameNS("*", "dest");
            if (destList.getLength() > 0) {
                Node dest = destList.item(0);
                dest.getParentNode().removeChild(dest);
            }

            /* Busca o valor da NFCe e substitui a tag pag por uma só */
            String valorNf = "";
            NodeList vnfList = doc.getElementsByTagNameNS("*", "vNF");
            if (vnfList.getLength() > 0) {
                valorNf = vnfList.item(0).getTextContent();
            }

            NodeList pagList = doc.getElementsByTagNameNS("*", "pag");
            if (pagList.getLength() > 0) {
                Node pag = pagList.item(0);

                while (pag.hasChildNodes()) {
                    pag.removeChild(pag.getFirstChild());
                }

                Element detPag = doc.createElementNS(namespace, "detPag");

                Element tPag = doc.createElementNS(namespace, "tPag");
                tPag.setTextContent("99");
                detPag.appendChild(tPag);

                Element xPag = doc.createElementNS(namespace, "xPag");
                xPag.setTextContent("Outros");
                detPag.appendChild(xPag);

                Element vPag = doc.createElementNS(namespace, "vPag");
                vPag.setTextContent(valorNf);
                detPag.appendChild(vPag);

                pag.appendChild(detPag);
            }

            // Remove todos os nós de texto que sejam só whitespace
            removerNosEmBranco(doc.getDocumentElement());
            // Serializa sem declaração e sem indentação (totalmente linearizado)
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            File outFile = new File(arquivoSaida);
            transformer.transform(new DOMSource(doc), new StreamResult(outFile));
            // Limpa arquivo original
            File file = new File(arquivoEntrada);
            file.delete();

            principal.adicionarLog("Arquivo gravado em: " + arquivoSaida);
        } catch (ParserConfigurationException ex) {
            principal.adicionarLog("Erro na configuração do parser XML: " + ex.getMessage());
        } catch (SAXException ex) {
            principal.adicionarLog("Erro no formato do XML: " + ex.getMessage());
        } catch (IOException ex) {
            principal.adicionarLog("Erro ao acessar o arquivo: " + ex.getMessage());
        } catch (TransformerConfigurationException ex) {
            principal.adicionarLog("Erro ao configurar o transformador: " + ex.getMessage());
        } catch (TransformerException ex) {
            principal.adicionarLog("Erro durante a transformação: " + ex.getMessage());
        }
    }

    /**
     * Remove todos os nós de texto que contenham apenas espaços em branco a
     * partir de um Element.
     */
    private static void removerNosEmBranco(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().isEmpty()) {
                    element.removeChild(child);
                    i--;
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                removerNosEmBranco((Element) child);
            }
        }
    }

    public static void autorizarXml(String arquivoEntrada, String arquivoSucesso, String arquivoFalha) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        // Carrega o XML como Document
        File xmlFile = new File(arquivoEntrada);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        // Converte o Document em uma String
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        String xml = writer.toString();

        String cnpjFilial = "";
        NodeList cnpjList = doc.getElementsByTagNameNS("*", "CNPJ");
        if (cnpjList.getLength() > 0) {
            cnpjFilial = cnpjList.item(0).getTextContent();
        }

        WebServiceSDE webServiceSDE = new WebServiceSDE();

        // Define o destino com base no resultado
        String destino = webServiceSDE.autorizarDocumento(xml, cnpjFilial) ? arquivoSucesso : arquivoFalha;
        File outFile = new File(destino);
        transformer.transform(new DOMSource(doc), new StreamResult(outFile));

        // Exclui o arquivo original
        File file = new File(arquivoEntrada);
        file.delete();
    }

}
