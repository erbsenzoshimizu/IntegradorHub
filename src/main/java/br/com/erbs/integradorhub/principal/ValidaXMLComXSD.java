package br.com.erbs.integradorhub.principal;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import java.io.*;

public class ValidaXMLComXSD {

    public static void validar(File xmlFile, File xsdFile) throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (!xsdFile.exists()) {
            throw new FileNotFoundException("Arquivo XSD não encontrado: " + xsdFile.getAbsolutePath());
        }

        // Configura um ResourceResolver para resolver imports com depuração
        factory.setResourceResolver(new LSResourceResolver() {
            @Override
            public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                System.out.println("Resolvendo import: type=" + type + ", namespaceURI=" + namespaceURI + 
                                   ", publicId=" + publicId + ", systemId=" + systemId + ", baseURI=" + baseURI);
                try {
                    // Tenta resolver o import como arquivo local na pasta C:\temp\nfcecancelamento
                    File importFile;
                    if (systemId != null && (systemId.endsWith("tiposBasico_v1.03.xsd") || 
                                             systemId.endsWith("xmldsig-core-schema_v1.01.xsd") || 
                                             systemId.endsWith("e110111_v1.00.xsd"))) {
                        importFile = new File("C:\\temp\\nfcecancelamento\\" + systemId);
                    } else {
                        // Evita tentar resolver URLs externas ou systemIds inesperados
                        System.out.println("Ignorando import não local: " + systemId);
                        return null;
                    }
                    if (importFile.exists()) {
                        System.out.println("Import encontrado: " + importFile.getAbsolutePath());
                        LSInput input = new LSInput() {
                            private InputStream byteStream;
                            private String systemId;

                            @Override
                            public InputStream getByteStream() { return byteStream; }
                            @Override
                            public void setByteStream(InputStream byteStream) { this.byteStream = byteStream; }
                            @Override
                            public String getSystemId() { return systemId; }
                            @Override
                            public void setSystemId(String systemId) { this.systemId = systemId; }
                            @Override
                            public String getPublicId() { return null; }
                            @Override
                            public void setPublicId(String publicId) {}
                            @Override
                            public String getBaseURI() { return null; }
                            @Override
                            public void setBaseURI(String baseURI) {}
                            @Override
                            public String getStringData() { return null; }
                            @Override
                            public void setStringData(String stringData) {}
                            @Override
                            public Reader getCharacterStream() { return null; }
                            @Override
                            public void setCharacterStream(Reader characterStream) {}
                            @Override
                            public String getEncoding() { return null; }
                            @Override
                            public void setEncoding(String encoding) {}
                            @Override
                            public boolean getCertifiedText() { return false; }
                            @Override
                            public void setCertifiedText(boolean certifiedText) {}
                        };
                        input.setByteStream(new FileInputStream(importFile));
                        input.setSystemId(importFile.toURI().toString());
                        return input;
                    } else {
                        System.err.println("Import não encontrado: " + importFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao resolver import: " + systemId);
                    e.printStackTrace();
                }
                return null;
            }
        });

        Schema schema = factory.newSchema(xsdFile);
        Validator validator = schema.newValidator();
        
        try {
            validator.validate(new StreamSource(xmlFile));
            System.out.println("✅ XML válido conforme o XSD!");
        } catch (SAXException e) {
            System.err.println("❌ XML inválido: " + e.getMessage());
            throw e;
        }
    }

    public static void main(String[] args) {
        File xml = new File("C:\\temp\\Canc53250517645625005091650020000000581418170067.xml");
        File xsd = new File("C:\\temp\\nfcecancelamento\\envEventoCancNFe_v1.00.xsd");

        try {
            validar(xml, xsd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}