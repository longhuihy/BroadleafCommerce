/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.core.search.service.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.search.domain.Field;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Service exposing several methods for creating a Solr index based on catalog product data.
 *
 * @see org.broadleafcommerce.core.search.service.solr.SolrIndexCachedOperation
 * @author Andre Azzolini (apazzolini)
 * @author Jeff Fischer
 */
public interface SolrIndexService {

    /**
     * Rebuilds the current index. 
     * 
     * @throws IOException 
     * @throws ServiceException
     */
    public void rebuildIndex() throws ServiceException, IOException;
    
    /**
     * Allows a query to determine if a full reindex is currently being performed. 
     * 
     * NOTE: There is no guarantee that reindexing is not happening in another process or another server.  This 
     * method simply indicates whether this instance, within a single JVM is currently performing a reindex.
     * 
     * @return
     */
    public boolean isReindexInProcess();

    /**
     * The internal method for building indexes. This is exposed via this interface in case someone would like to 
     * more granularly control the indexing strategy.
     * 
     * @see #restoreState(Object[])
     * @param page
     * @param pageSize
     * @param useReindexServer - if set to false will index directly on the primary server
     * @throws ServiceException
     */
    public void buildIncrementalIndex(int page, int pageSize, boolean useReindexServer) throws ServiceException;

    /**
     * This can be used in lieu of passing in page sizes,  The reason is that one might want to apply filters or only 
     * index certain skus.
     * @param skus
     * @param useReindexServer
     * @throws ServiceException
     */
    public void buildIncrementalSkuIndex(List<Sku> skus, boolean useReindexServer) throws ServiceException;

    /**
     * This can be used in lieu of passing in page sizes,  The reason is that one might want to apply filters or only 
     * index certain products.
     * @param products
     * @param useReindexServer
     * @throws ServiceException
     */
    public void buildIncrementalProductIndex(List<Product> products, boolean useReindexServer) throws ServiceException;

    /**
     * Saves some global context that might be altered during indexing.
     * 
     * @return
     */
    public Object[] saveState();

    /**
     * Restores state that was saved prior to indexing that might have been altered.
     * 
     * @see #saveState()
     * @param pack
     */
    public void restoreState(Object[] pack);

    /**
     * Triggers the Solr optimize index function on the given server.
     * 
     * NOTE: This should rarely be called.
     * 
     * @param server
     * @throws ServiceException
     * @throws IOException
     */
    public void optimizeIndex(SolrServer server) throws ServiceException, IOException;

    /**
     * Allows a commit to be called.  By default, the details of the commit will depend on system properties, including:
     * 
     * solr.index.commit - if false, then no commit will be performed. autoCommit (and autoSoftCommit) should be configured in Solr.
     * solr.index.softCommit - indicates if a soft commit should be performed
     * solr.index.waitSearcher - indicates if the process should wait for a searcher to be configured
     * solr.index.waitFlush - indicates if the process should wait for a flush to disk
     * 
     * @param server
     * @throws ServiceException
     * @throws IOException
     */

    public void commit(SolrServer server) throws ServiceException, IOException;

    /**
     * This allows an external caller to force a commit to the SolrServer.  See Solr Documentation for 
     * additional details.  If using softCommit, you should ensure that a hardCommit is performed, either 
     * using autoCommit, or at the end of the commit process to flush the changes to the disk.
     * 
     * Note that this method will force a commit even if solr.index.commit=false
     * 
     * @param server - the SolrServer to update
     * @param softCommit - soft commit is an efficient commit that does not write the data to the file system
     * @param waitSearcher - whether or not to wait for a new searcher to be created
     * @param waitFlush - whether or not to wait for a flush to disk.
     * @throws ServiceException
     * @throws IOException
     */
    public void commit(SolrServer server, boolean softCommit, boolean waitSearcher, boolean waitFlush) throws ServiceException, IOException;

    /**
     * Prints out the docs to the trace logger
     * 
     * @param documents
     */
    public void logDocuments(Collection<SolrInputDocument> documents);

    /**
     * @return a list of all possible locale prefixes to consider
     */
    public List<Locale> getAllLocales();

    /**
     * Given a product, fields that relate to that product, and a list of locales and pricelists, builds a 
     * SolrInputDocument to be added to the Solr index.
     * 
     * @param product
     * @param fields
     * @param locales
     * @return the document
     */
    public SolrInputDocument buildDocument(Product product, List<Field> fields, List<Locale> locales);

    /**
     * Given a sku, fields that relate to that sku, and a list of locales and pricelists, builds a 
     * SolrInputDocument to be added to the Solr index.
     * 
     * @param sku
     * @param fields
     * @param locales
     * @return the document
     */
    public SolrInputDocument buildDocument(Sku sku, List<Field> fields, List<Locale> locales);

    /**
     * SolrIndexService exposes {@link #buildIncrementalIndex(int, int, boolean)}.
     * By wrapping the call to this method inside of a {@link org.broadleafcommerce.core.search.service.solr.SolrIndexCachedOperation.CacheOperation},
     * a single cache will be used for all the contained calls to buildIncrementalIndex. Here's an example:
     * {@code
     *  performCachedOperation(new SolrIndexCachedOperation.CacheOperation() {
     *        @Override
     *        public void execute() throws ServiceException {
     *            int page = 0;
     *            while ((page * pageSize) < numProducts) {
     *                buildIncrementalIndex(page, pageSize);
     *                page++;
     *            }
     *        }
     *    });
     * }
     * Note {@link #rebuildIndex()} already internally wraps its call using CacheOperation, so it is not necessary to call performCacheOperation
     * for calls to rebuildIndex().
     *
     * @param cacheOperation the block of code to perform using a single cache for best performance
     * @throws ServiceException
     */
    public void performCachedOperation(SolrIndexCachedOperation.CacheOperation cacheOperation) throws ServiceException;
}