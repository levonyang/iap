package com.haizhi.iap.search.model;

import java.util.HashSet;
import java.util.Set;

public class VertexCollection
{
    public final static String PERSON = "Person";
    public final static String COMPANY = "Company";

    private static Set<String> VERTEX_COLLECTIONS = new HashSet<>();

    static {
        VERTEX_COLLECTIONS.add(PERSON);
        VERTEX_COLLECTIONS.add(COMPANY);
    }

    public static boolean contains(String vertexCollection)
    {
        return VERTEX_COLLECTIONS.contains(vertexCollection);
    }
}
