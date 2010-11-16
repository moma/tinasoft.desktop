var dataset = {
  nodes:[
    {nodeName:"period1", group:1},
    {nodeName:"keyphrase1", group:1},
    {nodeName:"keyphrase2", group:1},
    {nodeName:"document1", group:1},
    {nodeName:"period2", group:1},
    {nodeName:"document2", group:1},
  ],
  links:[
    {source:0, target:1, value:1},
    {source:1, target:0, value:1},
    {source:0, target:2, value:1},
    {source:2, target:0, value:1},
    {source:0, target:3, value:1},
    {source:3, target:0, value:1},
    {source:1, target:3, value:5},
    {source:3, target:1, value:5},
    {source:2, target:3, value:10},
    {source:3, target:2, value:10},
    {source:5, target:3, value:1},
    {source:3, target:5, value:1},
    {source:2, target:5, value:15},
    {source:5, target:2, value:15},
    {source:4, target:5, value:1},
    {source:5, target:4, value:1},
  ]
};
