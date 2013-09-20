<?xml version="1.0" encoding="iso-8859-1" standalone="no" ?>
<!-- DOCTYPE xsl:stylesheet SYSTEM ""-->
<!-- $Id: form-align.xsl 795 2008-08-28 15:53:19Z euzenat $ -->

<xsl:stylesheet version="1.0"
  xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'
  xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output
  method="html"
  encoding="iso-8859-1"
  omit-xml-declaration="no"
  standalone="no"
  doctype-public="-//IETF//DTD HTML//EN"
  indent="yes"/> 

<xsl:template match="/">
  <html><head></head><body bgcolor="white">
  <xsl:apply-templates select="rdf:RDF/align:Alignment"/>
  </body></html>
</xsl:template>

<xsl:template match="align:Alignment">
  <!-- if no xml then decline -->
  <h1>(level <xsl:value-of select="align:level/text()"/>)
  Alignment</h1>
  <h2>Source:
    <xsl:element name="a">
      <xsl:attribute name="href">
	<xsl:value-of select="align:uri1/text()"/>
      </xsl:attribute>
      <xsl:value-of select="align:onto1/text()"/>
    </xsl:element></h2>
  <h2>Target:
    <xsl:element name="a">
      <xsl:attribute name="href">
	<xsl:value-of select="align:uri2/text()"/>
      </xsl:attribute>
      <xsl:value-of select="align:onto2/text()"/>
    </xsl:element></h2>
  <h2>Correspondences</h2>
  <dl compact="1">
  <xsl:apply-templates select="align:map/align:Cell"/>
  </dl>
</xsl:template>

<xsl:template match="align:Cell">
  <!-- it is a pity to retrieve the local name this way.
       but the XSLT local-name() function works on node-sets
       and the result of the value-of is a string.
       This can be handled by the :evaluate() function of saxon or xalan
       but is implementation depend (wait for XSLT 2) -->
    <xsl:variable name="elt1"><xsl:value-of select="align:entity1/@rdf:resource"/></xsl:variable>
    <xsl:variable name="elt2"><xsl:value-of select="align:entity2/@rdf:resource"/></xsl:variable>
  <dt>
    <xsl:value-of select="substring-after($elt1,'#')"/><xsl:text> </xsl:text>
  <xsl:value-of select="align:relation/text()"/><xsl:text> </xsl:text>
  <xsl:value-of select="substring-after($elt2,'#')"/></dt>
  <dd><xsl:value-of select="align:measure/text()"/></dd>
</xsl:template>

</xsl:stylesheet>
