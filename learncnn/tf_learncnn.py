#!/usr/bin/env python
import tensorflow as tf
import numpy as np
from math import sqrt
import numpy as np
from numpy import zeros
import sys
import re
import math
import os
from tf_model_component import *
from batch_object import batch_object
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
import pdb
from tensorflow.python.saved_model import builder as saved_model_builder

# not defined by the parameter file
weight_decay = 5e-4
learning_rate = 1e-4
output_step = 10

def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

def encode(s):
    ns = s.upper()
    pattern = re.compile(r'\s+')
    ns = re.sub(pattern, '', ns)
    ns = ns.replace("A", "0,")
    ns = ns.replace("T", "1,")
    ns = ns.replace("G", "2,")
    ns = ns.replace("C", "3,")
    if re.search('[a-zA-Z]', ns):
        print(s)
    	print('Non-standard symbol in sequence - changed to A.')
    	ns = re.sub("[a-zA-Z]", "0,", ns)
    return ns[:-1]

np.random.seed(2504) 

totalOption = len(sys.argv)
if totalOption != 2:
    print('USAGE: <parameters file>')
    exit(0)

p = {'maxlen':81, 'batch_size':16, 'hidden_dims':128, 'nb_epoch':5,
'pos_seq':'post.fa', 'neg_seq':'negt.fa', 'model_output':'model.h5', 'learning_split':0.6,
'testing_split':0.3,'validation_split':0.1,
 'patience':4,  'read_model': 0, 'test_results_file':'results', 'threshold':0, 'model_file': 'model.h5',
   'architecture':[[200, 21, 2]]  }
with open(sys.argv[1]) as f:
    for line in f:
        if(not line.strip().startswith("#") and len(line)>0):
            a = line.split("=")
            k = a[0].strip()
            v = a[1].strip()
            if k in p:
                if k=='architecture':
                    p[k]= np.fromstring(v, dtype=int, sep=',').reshape((-1, 3))
                elif v.isdigit():
                    p[k] = int(v)
                elif is_number(v):
                    p[k] = float(v)
                else:
                    p[k] = v

     
max_features = 4  
pos_seq = []
seq = ""
with open(p['pos_seq']) as f:
    for line in f:
        if(line.startswith(">")):
            if(len(seq)!=0):
                pos_seq.append(np.fromstring(encode(seq), dtype=int, sep=","))
                seq=""                    
            continue                
        else:
            seq+=line
if(len(seq)!=0):
    pos_seq.append(np.fromstring(encode(seq), dtype=int, sep=","))

neg_seq = []
seq = ""
with open(p['neg_seq']) as f:
    for line in f:
        if(line.startswith(">")):
            if(len(seq)!=0):
                neg_seq.append(np.fromstring(encode(seq), dtype=int, sep=","))
                seq=""                    
            continue                
        else:
            seq+=line
if(len(seq)!=0):
    neg_seq.append(np.fromstring(encode(seq), dtype=int, sep=","))



np.random.shuffle(pos_seq)
np.random.shuffle(neg_seq)

lpn = int(math.floor((p['learning_split'] + p['validation_split'])*len(pos_seq)))
lnn = int(math.floor((p['learning_split'] + p['validation_split'])*len(neg_seq)))
tpn = int(math.ceil(p['testing_split']*len(pos_seq)))
tnn = int(math.ceil(p['testing_split']*len(neg_seq)))

X_train = zeros((lpn + lnn,  len(pos_seq[0]), 4))
y_train = zeros((lpn + lnn, 2))
X_test = zeros((tpn + tnn,  len(pos_seq[0]), 4))
y_test = zeros((tpn + tnn, 2))

for i in range(lpn):
    y_train[i] = (1, 0)
    for j in range(len(pos_seq[0])):
        X_train[i][j][pos_seq[i][j]]=1

for i in range(lnn):
    y_train[lpn + i] = (0, 1)
    for j in range(len(neg_seq[0])):
        X_train[lpn + i][j][neg_seq[i][j]]=1

for i in range(tpn):
    y_test[i] = (1, 0)
    for j in range(len(pos_seq[0])):
        X_test[i][j][pos_seq[lpn + i][j]]=1

for i in range(tnn):
    y_test[tpn + i] = (0, 1)
    for j in range(len(neg_seq[0])):
        X_test[tpn + i][j][neg_seq[lnn + i][j]]=1

print('Parameters File: ' + sys.argv[1])
print('Positives: ' + p['pos_seq'] + '   ' + str(len(pos_seq)))
print('Negatives: ' + p['neg_seq'] + '   ' + str(len(neg_seq)))
print('Positives for training: ' + str(lpn))
print('Negatives for training: ' + str(lnn))
print('Positives for testing:  ' + str(tpn))
print('Negatives for testing:  ' + str(tnn))

# define the graph
def model_graph(x):
	weight_dict = dict()
	bias_dict = dict()
	channel_dict = dict()
	channel_dict[-1] = 4
	out_dict = dict()
	out_dict[-1] = x

	

	for i in range(len(p['architecture'])):
		a = p['architecture'][i]
		with tf.name_scope('conv_layers_{}'.format(i)):
			channel_dict[i] = a[0]
			filter_length = a[1]
			weight_dict[i] = weight_variable([filter_length,
				channel_dict[i-1], channel_dict[i]])
			bias_dict[i] = bias_variable([channel_dict[i]])
			out_dict[i] = tf.nn.conv1d(out_dict[i-1], 
				weight_dict[i], stride=1, padding='VALID') + bias_dict[i]
			out_dict[i] = tflearn.batch_normalization(out_dict[i])
			out_dict[i] = tf.nn.relu(out_dict[i])

			if a[2] > 0:
				sv = int(a[2])
				out_dict[i] = tf.nn.pool(out_dict[i], window_shape=[sv], 
					pooling_type='MAX', padding='VALID')
	
	with tf.name_scope('fc_layer'):
		flatten = tf.contrib.layers.flatten(out_dict[i])
		fc_in_dim = flatten.get_shape().as_list()[-1]
		weight_dict['fc'] = weight_variable([fc_in_dim, p['hidden_dims']])
		bias_dict['fc'] = bias_variable([p['hidden_dims']])
		out_dict['fc'] = tf.matmul(flatten, 
			weight_dict['fc'])+bias_dict['fc']
		out_dict['fc'] = tflearn.batch_normalization(out_dict['fc'])
		out_dict['fc'] = tf.nn.relu(out_dict['fc'])

	with tf.name_scope('softmax_layer'):
		weight_dict['soft'] = weight_variable([p['hidden_dims'], 2])
		bias_dict['soft'] = bias_variable([2])
		out_dict['out_logit'] = tf.matmul(out_dict['fc'], 
			weight_dict['soft'])+bias_dict['soft']
		out_dict['out'] = tf.nn.softmax(out_dict['out_logit'])

	# pdb.set_trace()
	return out_dict

# define the optimizer and so on and do a random initialization of the graph
def dl_model(x_train, y_train, x_test, y_test, load_model):
	batch_size = p['batch_size']
	nb_epoch=p['nb_epoch']

	x_train, x_val, y_train, y_val = train_test_split(x_train, y_train, 
		test_size=float(p['validation_split'])/(p['learning_split']+p['validation_split']))

	# notice here I used the length of x_train as the length instead of
	# maxlen
	input_x = tf.placeholder(tf.float32,shape=[None, np.shape(x_train)[1], 
		max_features], name='input_java')
	y_ = tf.placeholder(tf.float32, [None, 2])

	out_dict = model_graph(input_x)
	y_logit = out_dict['out_logit']	
	y = out_dict['out']
	tf.identity(y, name="output_java")
	#tf.identity(y, name="name4")
	# pdb.set_trace()

	# Define loss and optimizer
	cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(
		logits=y_logit, labels=y_))

	weight_collection=[v for v in tf.get_collection(
		tf.GraphKeys.TRAINABLE_VARIABLES) if v.name.endswith('weights:0')]
	l2_loss = tf.add_n([tf.nn.l2_loss(v) for v in weight_collection])
	cost = tf.add(cost, weight_decay*l2_loss)
	optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate).minimize(cost)

	# Evaluate model
	predicted_label = tf.argmax(y_logit, 1)
	correct_pred = tf.equal(tf.argmax(y_logit, 1), tf.argmax(y_, 1))
	accuracy = tf.reduce_mean(tf.cast(correct_pred, tf.float32))

	# Start the session
	sess = tf.InteractiveSession(config=tf.ConfigProto(
		log_device_placement=False,allow_soft_placement=True))

	# Initializing the variables
	sess.run(tf.global_variables_initializer())

	if load_model:
		saver = tf.train.Saver()
		saver.restore(sess, './'+p['model_file'])

	def set_check(test_feature, test_label, batch_size):
	    predict_test_label=[]
	    prob_test = []
	    number_of_full_batch=int(math.floor(len(test_feature)/batch_size))

	    for i in range(number_of_full_batch):
	        prob_out, predicted_label_out = sess.run([y, predicted_label],
	            feed_dict={input_x: test_feature[i*batch_size:(i+1)*batch_size],
	            y_: test_label[i*batch_size:(i+1)*batch_size]})
	        prob_test += list(prob_out)
	        predict_test_label+=list(predicted_label_out)
	    
	    prob_out, predicted_label_out = sess.run([y, predicted_label],
	        feed_dict={input_x: test_feature[number_of_full_batch*batch_size:], 
	        y_: test_label[number_of_full_batch*batch_size:]})
	    

	    prob_test += list(prob_out)
	    predict_test_label+=list(predicted_label_out)

	    true_label = np.argmax(test_label, axis=1)
	    acc_score=accuracy_score(true_label,np.array(predict_test_label))
	    print('The accuracy for the whole set is {}'.format(acc_score))

	    return (prob_test, predict_test_label)

	if not load_model:
		for epoch in range(nb_epoch):
			# x_train_obj = batch_object(x_train, batch_size)
			# y_train_obj = batch_object(y_train, batch_size)
			for step in range(int(len(x_train)/batch_size)+1):
				# x_train_batch = x_train_obj.next_batch()
				# y_train_batch = y_train_obj.next_batch()
				train_batch = generate_random_batch([x_train], y_train, batch_size)
				sess.run(optimizer, feed_dict={
					input_x: train_batch[0][0],
					y_: train_batch[1]
					})				
				if step%output_step ==0:
					loss, acc = sess.run([cost, accuracy], feed_dict = {
						input_x: train_batch[0][0],
						y_: train_batch[1]
					})
					print('Train step %d'%step)
					print('Train loss: %f, train acc: %f'%(loss, acc))
					# pdb.set_trace()
					test_batch = generate_random_batch([x_val], y_val, batch_size)
					loss, acc = sess.run([cost, accuracy], feed_dict ={
						input_x: test_batch[0][0],
						y_: test_batch[1]
						})
					print('Val loss: %f, val acc: %f'%(loss, acc))
			print('Result after training epoch {}'.format(epoch))
			print('Training set')
			set_check(x_train, y_train, batch_size)
			print('Validation set')
			set_check(x_val, y_val, batch_size)

		# define the model storage part
		builder = tf.saved_model.builder.SavedModelBuilder("./"+p['model_output'])
		builder.add_meta_graph_and_variables(sess, [tf.saved_model.tag_constants.SERVING])
		builder.save(True)

	print('Result on test set')
	prob, test_label_pred = set_check(x_test, y_test, batch_size)

	return prob


# If the model exist go to load and test
# If the model does not exist, go to the train and test
if (p['read_model'] == 1):
	predict = dl_model(X_train, y_train, X_test, y_test, True)
else:
	predict = dl_model(X_train, y_train, X_test, y_test, False)

a = zeros(len(y_test))
tv = p['threshold']

for i in range(len(predict)):
    if predict[i][0] - predict[i][1] + tv > 0:
        a[i] = 1
np.savetxt(p['test_results_file'], a, newline="\n")

tp = 0.0
tn = 0.0
fp = 0.0
fn = 0.0

for i in range(len(y_test)):
    if(y_test[i][0] == 1):
        if(a[i] == 1):
            tp+=1
        else:
            fn+=1
    if(y_test[i][0] == 0):
        if(a[i] == 1):
            fp+=1
        else:
            tn+=1
print('')
print('TP: '+str(tp))
print('TN: '+str(tn))
print('FP: '+str(fp))
print('FN: '+str(fn))
print('Test accuracy: ' + str((tp+tn)/(tp+tn+fp+fn)))
sn = tp/(tp+fn)
sp = tn/(tn+fp)
mcc = (tp*tn - fp*fn)/sqrt((tp+fp)*(tp+fn)*(tn+fp)*(tn+fn))
print('Sensitivity: ' + str(sn))
print('Specificity: ' + str(sp))
print('CC: ' + str(mcc))

















