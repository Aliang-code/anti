package dna.top.service;

import dna.entity.top.DocInfo;
import dna.persistence.factory.DAOFactory;
import dna.top.dao.DocInfoDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DocInfoService {
    private static final Logger logger = LoggerFactory.getLogger(DocInfoService.class);

    public void addDocInfo(List<DocInfo> docInfos) {
        DocInfoDAO docInfoDAO = DAOFactory.getDAO(DocInfoDAO.class);
        docInfoDAO.saveBatch(docInfos);
    }

    public DocInfo getDocInfo(int docId) {
        DocInfoDAO docInfoDAO = DAOFactory.getDAO(DocInfoDAO.class);
        return docInfoDAO.get(docId);
    }
}
