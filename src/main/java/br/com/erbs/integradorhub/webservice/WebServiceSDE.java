package br.com.erbs.integradorhub.webservice;

import br.com.erbs.integradorhub.dao.NotaFiscalSaidaDAO;
import br.com.erbs.integradorhub.principal.Principal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class WebServiceSDE {
    
    private static final String BASE_URL = "http://srv-spnsteste3:8989/SDE/Integracao";
    private static final String NAMESPACE = "http://www.senior.com.br/nfe";
    private static final String USER = "edocs";
    private static final String PASSWORD = "r3m0t0-";
    private final Principal principal;
    
    public WebServiceSDE() {
        principal = new Principal();
    }
    
    private Dispatch<SOAPMessage> createDispatch(String endpoint) throws Exception {
        URL url = new URL(BASE_URL + "?wsdl");
        QName serviceName = new QName(NAMESPACE, "IntegracaoDocumentoServico");
        Service service = Service.create(url, serviceName);
        QName portQName = new QName(NAMESPACE, "BasicHttpBinding_IIntegracaoDocumento");
        Dispatch<SOAPMessage> dispatch = service.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);

        // Configura o BindingProvider para garantir o uso do SOAPAction
        BindingProvider bp = (BindingProvider) dispatch;
        bp.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        return dispatch;
    }
    
    private SOAPMessage createSoapMessage(String operation, String[][] parameters) throws Exception {
        // 1) Cria a mensagem e ajusta prefixes
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage soapMessage = factory.createMessage();
        SOAPPart part = soapMessage.getSOAPPart();
        SOAPEnvelope env = part.getEnvelope();
        env.setPrefix("soapenv");
        env.getHeader().setPrefix("soapenv");
        env.getBody().setPrefix("soapenv");

        // 2) Monta o corpo <nfe:AutorizarDocumento>
        SOAPBody body = env.getBody();
        SOAPElement root = body.addChildElement(operation, "nfe", NAMESPACE);

        // 3) Para cada parâmetro, insere como nó normal ou CDATA no caso de "xml"
        Document doc = part; // o SOAPPart também implementa org.w3c.dom.Document
        for (String[] param : parameters) {
            String name = param[0], value = param[1];
            SOAPElement el = root.addChildElement(name, "nfe", NAMESPACE);
            
            if ("xml".equals(name)) {
                // value aqui deve ser só o xml puro, sem <![CDATA[…]]>
                CDATASection cdata = doc.createCDATASection(value);
                el.appendChild(cdata);
            } else {
                el.setTextContent(value);
            }
        }

        // 4) Ajusta o SOAPAction
        String soapAction = NAMESPACE + "/IIntegracaoDocumento/" + operation;
        soapMessage.getMimeHeaders().addHeader("SOAPAction", soapAction);
        
        soapMessage.saveChanges();
        return soapMessage;
    }
    
    public Boolean autorizarDocumento(String xml, String cnpjFilial) {
        try {
            Dispatch<SOAPMessage> dispatch = createDispatch("AutorizarDocumento");

            // Define o SOAPAction explicitamente no BindingProvider
            BindingProvider bp = (BindingProvider) dispatch;
            bp.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, NAMESPACE + "/IIntegracaoDocumento/AutorizarDocumento");
            
            String[][] params = {
                {"usuario", USER},
                {"senha", PASSWORD},
                {"cnpjFilial", cnpjFilial},
                {"xml", xml},
                {"identificacaoGerador", "ERP Senior"}
            };
            
            SOAPMessage request = createSoapMessage("AutorizarDocumento", params);
            SOAPMessage response = dispatch.invoke(request);
            
            Map<String, String> resposta = new HashMap<>();
            SOAPBody responseBody = response.getSOAPBody();
            
            NodeList nodes;
            
            nodes = responseBody.getElementsByTagNameNS("*", "Sucesso");
            if (nodes.getLength() > 0) {
                resposta.put("Sucesso", nodes.item(0).getTextContent());
            }
            
            nodes = responseBody.getElementsByTagNameNS("*", "Mensagem");
            if (nodes.getLength() > 0) {
                resposta.put("Mensagem", nodes.item(0).getTextContent());
            }
            
            nodes = responseBody.getElementsByTagNameNS("*", "ProtocoloAutorizacao");
            if (nodes.getLength() > 0) {
                resposta.put("ProtocoloAutorizacao", nodes.item(0).getTextContent());
            }
            
            nodes = responseBody.getElementsByTagNameNS("*", "DataAutorizacao");
            if (nodes.getLength() > 0) {
                resposta.put("DataAutorizacao", nodes.item(0).getTextContent());
            }
            
            nodes = responseBody.getElementsByTagNameNS("*", "ChaveDocumento");
            if (nodes.getLength() > 0) {
                resposta.put("ChaveDocumento", nodes.item(0).getTextContent());
            }
            
            if ("true".equals(resposta.get("Sucesso"))) {
                NotaFiscalSaidaDAO notaFiscalSaidaDAO = new NotaFiscalSaidaDAO();
                notaFiscalSaidaDAO.autorizarNfce(resposta);
                
                principal.adicionarLog("Documento autorizado com sucesso.", null);
                
                return true;
            } else if ("false".equals(resposta.get("Sucesso"))) {
                principal.adicionarLog("Erro ao autorizar documento: " + resposta.get("Mensagem"), "erro");
                return false;
            } else {
                principal.adicionarLog("Resposta sem status definido.", "erro");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
