package com.haizhi.iap.search;

/**
 * Created by thomas on 18/3/29.
 *
 * 图的环路检测
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
public class TestLoopDetection
{
   /* @Autowired
    private GraphService graphService;

    @Autowired
    private GraphCircuitDetector graphCircuitDetector;

    private final String COMPANY_NAME = "兰州银行股份有限公司";
    private GraphVo graph;

    @Before
    public void setUpGraph()
    {
        graph = graphService.stockRight(COMPANY_NAME, GraphEdge.Direction.IN, 3);
        Assert.assertNotNull(graph);
    }

    @Test
    public void testLoopDetection()
    {
        Map<String, Map<String, Object>> idVertexMap = new HashMap<>();
        graph.getVertexes().forEach(vertex -> {
            idVertexMap.putIfAbsent(vertex.get("_id").toString(), vertex);
        });

        //set up the graph
        Graph<Map<String, Object>, Map<String, Object>> directedGraph = new DirectedSparseMultigraph<>();
        idVertexMap.values().forEach(directedGraph::addVertex);
        graph.getEdges().forEach(edge -> {
            Map<String, Object> from = idVertexMap.get(edge.get("_from").toString());
            Map<String, Object> to = idVertexMap.get(edge.get("_to").toString());
            directedGraph.addEdge(edge, from, to);
        });

        graphCircuitDetector.dfsDetect(graph.getVertexes().get(0), directedGraph);
    }*/
}
