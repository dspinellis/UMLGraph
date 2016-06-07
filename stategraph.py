#!/usr/bin/env python

from graphviz import Digraph
from pyparsing import *
import argparse
import os

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

    """ Creates the state based on the arguments given

    :param args_list:

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

    """ Creates the transition based on the arguments given

    :param args_list :

        args_list[0]: Source state identifier(MANDATORY).
        args_list[1]: Target state identifier(MANDATORY).
        args_list[2]: Caption to be displayed near the transition(OPTIONAL).
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

    """ Takes the list that contains the styling arguments to be applied
    and turns it to a dictionary which graphviz understands
    """

    changed_dict = {}
    for each_item in alist:
        a = each_item.split("=")
        changed_dict[a[0]] = a[1]

    return changed_dict


def parse_and_draw(graph, script):

    """ Parses every line of the script and calls
    an add method depending on that line's action command

    :param graph: current graph being used

    :param script: txt file being parsed
    """

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

        return graph

    except Exception as e:

        print "could not draw diagram because: " + str(e)


def render_graph(cmd_args, graph):

    """ Renders the graph and generates the output files as specified by the user
        If an output file is not specified. A pdf is generated using
        the name of the input file.
        The extension dot is erased because the format function can't handle it.

    :param cmd_args: contains the input file, the viewing preference and the output file
    :param graph: graph to render
    """

    if cmd_args.output is not None:
        filename, file_extension = os.path.splitext(cmd_args.output)

    else:
        filename = os.path.splitext(cmd_args.filename)[0]
        file_extension = 'pdf'

    graph.format = file_extension.replace('.', '')

    graph.render('test-output/' + filename, view=cmd_args.view)


# parse cmd arguments
parser = argparse.ArgumentParser()
parser.add_argument("filename")
parser.add_argument("-v", "--view", action="store_true", default=False)
parser.add_argument("-o", "--output", type=str)

args = parser.parse_args()

with open(args.filename) as f:
    content = f.read().splitlines()

script = "\n".join(content)

g1 = Digraph('g1', node_attr={'shape': 'box', 'style': 'rounded'})
g1.body.extend(['rankdir=LR'])  # graph's direction left-->right

render_graph(args, parse_and_draw(g1, script))
