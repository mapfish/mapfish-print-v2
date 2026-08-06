<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="1.0">
  <!-- Some XSLT kungfu for adjusting line widths by zoomFactor in SVG files -->
  <xsl:param name="zoomFactor">1</xsl:param>

  <xsl:preserve-space elements="text"/>

  <xsl:template match="/*">
    <svg:svg xmlns:svg="http://www.w3.org/2000/svg"
             xmlns:xlink="http://www.w3.org/1999/xlink">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name(.)}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </svg:svg>
  </xsl:template>

  <xsl:template match="*">
    <xsl:element name="svg:{name(.)}">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*"/>
      <xsl:apply-templates select="text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@stroke-width|@rx|@ry|@font-size">
    <xsl:attribute name="{name(.)}">
      <xsl:call-template name="factorValue">
        <xsl:with-param name="val" select="."/>
      </xsl:call-template>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@stroke-dasharray">
    <xsl:attribute name="{name(.)}">
      <xsl:call-template name="factorArray">
        <xsl:with-param name="vals" select="."/>
      </xsl:call-template>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:attribute name="{name(.)}">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- CustomXPath templates to separate line width values and multiply by zoomFactor param -->
  <xsl:template name="factorArray">
    <xsl:param name="vals"/>
    <xsl:variable name="item" select="normalize-space(substring-before(concat($vals,','), ','))"/>
    <xsl:variable name="rest" select="substring-after($vals, ',')"/>

    <xsl:call-template name="factorValue">
      <xsl:with-param name="val" select="$item"/>
    </xsl:call-template>

    <xsl:if test="string-length(normalize-space($rest)) > 0">
      <xsl:text>,</xsl:text>
      <xsl:call-template name="factorArray">
        <xsl:with-param name="vals" select="$rest"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="factorValue">

    <xsl:param name="val"/>

    <xsl:variable name="trimmed"
                  select="normalize-space($val)"/>

    <xsl:choose>
      <xsl:when test="$trimmed = 'none'">
        <xsl:value-of select="$trimmed"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="unit"
                      select="translate($trimmed, '0123456789.-', '')"/>
        <xsl:variable name="numStr">
          <xsl:choose>
            <xsl:when test="string-length($unit) > 0">
              <xsl:value-of select="substring($trimmed, 1, string-length($trimmed) - string-length($unit))"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$trimmed"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:variable name="scaled" select="number($numStr) * $zoomFactor"/>

        <xsl:variable name="intPart"  select="floor($scaled)"/>
        <xsl:variable name="fracPart" select="$scaled - $intPart"/>

        <xsl:choose>
          <xsl:when test="$fracPart = 0">
            <xsl:value-of select="$intPart"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$scaled"/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:value-of select="$unit"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>