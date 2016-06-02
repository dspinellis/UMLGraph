"""

description:

    This script allows the declarative specification of uml state diagrams.

    1) use the commands stated below to form a state diagram and save it in a text file
    2) change your directory to the place where both the script and the text file are located
    3) provide the arguments as stated below
    4) at the test-output directory you will find the diagram both in dot format and in pdf.

cmd args:

    argv[1]: input file name. (Mandatory)
    argv[2]: output file name. (Optional). Defaults to the input file's name
    argv[3]: view. (Optional). If true the generated pdf opens immediately. Defaults to False

commands:

    state: creates a state
    arguments: id (mandatory), name (mandatory), events[](optional), style[](optional

    transition: creates a transition
    arguments: source (mandatory), target (mandatory), label (optional), styling[] (optional)

    initial: the initial node. To be referred as initial

    final: the final node. To be referred as final

    compound_state: creates a subgraph which includes states and transitions
    arguments: name (optional)

    compound_end: ends the subgraph

    styles: applies styling to the element specified
    arguments: element (mandatory), styling[] (mandatory)

    note: applies a note to the state specified
    arguments: node_id (mandatory), note (mandatory)

"""


from graphviz import Digraph
from pyparsing import *
import sys


# -----Parser-----

LP, RP, LB, RB = map(Suppress, "()[]")  # those will be skipped when iterating
tokens = "+" + "'" + "=" + "/" + "_"

item = originalTextFor(OneOrMore(Word(alphanums + tokens)))  # originalText keeps the whitespaces between words

table = LB + item + ZeroOrMore(Suppress(Literal(",")) + item) + RB  # table: [item, item, ...]

argument = item | Group(table)  # argument = item or [item, item, ...]

listOfItems = Optional(argument + ZeroOrMore(Suppress(Literal(",")) + argument))  # LoI: [argument, argument, ...]

sentence = item.setResultsName("action") + LP + listOfItems.setResultsName("args") + RP  # sentence: item(LoI)

rule = OneOrMore(Group(sentence))  # rule: sentence, sentence, ...


def add_state(graph, args_list):
    """
    Creates the state based on the arguments given

    Args of args_list:
        args_list[0]: Unique identifier for the state inside the source(MANDATORY).
        args_list[1]: Caption to be displayed (defaults to the state id).
        args_list[2]: Events of the state(OPTIONAL).
    """

    if len(args_list) > 3 or len(args_list) == 0:
        raise Exception("states must follow the pattern: (id (mandatory), name (mandatory), "
                        "events[](optional), style[](optional))")
    else:

        if len(args_list) == 2:
            graph.node(args_list[0], args_list[1])

        elif len(args_list) == 3:
            graph.node(args_list[0], shape="record", label="<f0>" + args_list[1] + "|<f1>" +
                                                           '\\n'.join([str(lst) for lst in args_list[2]]))


def add_transition(graph, args_list):
    """
    Creates the transition based on the arguments given

    Args of args_list:
        args_list[0]: Start state identifier(MANDATORY).
        args_list[1]: End state identifier(MANDATORY).
        args_list[2]: Caption to be displayed near the edge(OPTIONAL).
        args_list[3]: Any styling to be applied(OPTIONAL).
    """

    if len(args_list) > 4 or len(args_list) < 2:
        raise Exception("transitions must follow the pattern: "
                        "(source (mandatory), target (mandatory), label (optional), styling[] (optional))")
    else:

        if len(args_list) == 2:
            graph.edge(args_list[0], args_list[1])

        elif len(args_list) == 3:
            graph.edge(args_list[0], args_list[1], args_list[2])

        else:
            # turns the args_list to a styling dictionary
            styles = list_to_dict(args_list[3])

            graph.edge(args_list[0], args_list[1], args_list[2], styles)


def apply_styles(graph, args_list):
    element = args_list[0]  # where the styling will be applied (graph, node, edge)
    styles = list_to_dict(args_list[1])

    if element == 'graph':
        graph.graph_attr.update(styles)
    elif element == 'node':
        graph.node_attr.update(styles)
    elif element == 'edge':
        graph.edge_attr.update(styles)
    else:
        raise Exception("styling must be applied to either graphs, nodes or edges")


def add_note(graph, args_list):
    note_name = "note" + args_list[0]  # unique note name
    graph.node(note_name, args_list[1], shape="note")
    graph.edge(note_name, args_list[0], arrowhead="none", style="dashed")


def initial(graph):
    graph.node("initial", shape="circle", style="filled", fillcolor="black", label="", width='0.3')


def final(graph):
    graph.node("final", shape="doublecircle", style="filled", fillcolor="black", label="", width='0.3')


def compound_state(args_list):
    g2 = Digraph('cluster_g2')  # subgraph's name MUST start with cluster_
    g2.body.extend(['rankdir=LR'])
    g2.body.append('color=black')
    g2.body.append('style=rounded')

    if args_list:
        label = args_list[0]
        g2.body.append('labeljust=center')
        g2.body.append('label="' + label + '"')

    return g2


def list_to_dict(alist):
    """
    takes the list that contains the styling arguments to be applied
    and turns it to a dictionary which graphviz understands
    """

    changed_dict = {}
    for each_item in alist:
        a = each_item.split("=")
        changed_dict[a[0]] = a[1]

    return changed_dict


# Parses every line of the script and calls an add method depending on that line's action command

def parse_and_draw(graph, script):
    entry = graph
    try:

        for line in rule.parseString(script):

            if line.action == "initial":
                initial(graph)

            if line.action == "final":
                final(graph)

            if line.action == "state":

                try:
                    add_state(graph, line.args)

                except Exception as e:
                    raise Exception(str(e))

            if line.action == "transition":

                try:
                    add_transition(graph, line.args)
                except Exception as e:
                    raise Exception(str(e))

            if line.action == "note":
                add_note(graph, line.args)

            if line.action == "styles":
                apply_styles(graph, line.args)

            if line.action == "compound_state":
                graph = compound_state(line.args)

            if line.action == "compound_end":
                entry.subgraph(graph)
                graph = entry

        # checks if user wants to view the graph right away
        if len(sys.argv) == 4:
            accepted_values = ['true', 'TRUE', 'True']
            if sys.argv[3] in accepted_values:
                view = True
            else:
                raise Exception("no suitable value for view")
        else:
            view = False

        if sys.argv[2]:  # if the user has specified an output file
            graph.render('test-output/'+str(sys.argv[2])+'.gv', view=view)
            'test-output/'+str(sys.argv[2])+'.pdf'
        else:  # else use the input file's name
            graph.render('test-output/'+str(sys.argv[1])+'.gv', view=view)
            'test-output/'+str(sys.argv[1])+'.pdf'

    except Exception as e:

        print "could not draw diagram because: " + str(e)


# Reads data from the input file

with open(str(sys.argv[1])) as f:
    content = f.read().splitlines()

script = "\n".join(content)

g1 = Digraph('g1', node_attr={'shape': 'box', 'style': 'rounded'})
g1.body.extend(['rankdir=LR'])  # graph's direction left-->right

parse_and_draw(g1, script)
