# Add a checkpoint callback to store the checkpoint that has the highest validation accuracy.
checkpoint_path = "models/weights.best.hdf5"
checkpoint = ModelCheckpoint(
    checkpoint_path,
    monitor ='val_accuracy',
    verbose = 1,
    save_best_only = True,
    mode ='max'
)

earlystopping = EarlyStopping(monitor ='val_accuracy', patience = 20)

# Start training
model_history = model.fit(
    processed_X_train, 
    y_train,
    epochs = 200,
    batch_size = 16,
    validation_data = (processed_X_val, y_val),
    callbacks = [
         checkpoint, 
         earlystopping
    ]
)