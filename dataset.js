var dataset = {
  nodes:[
    {nodeName:"period", group:1},
    {nodeName:"keyphrase1", group:1},
    {nodeName:"keyphrase2", group:1},
    {nodeName:"document", group:1},
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
    {source:2, target:3, value:2},
    {source:3, target:2, value:2},
  ]
};
