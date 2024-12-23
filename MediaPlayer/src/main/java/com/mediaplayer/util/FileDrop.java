package com.mediaplayer.util;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

public class FileDrop {
    public interface FileDropListener {
        void filesDropped(File[] files);
    }

    public FileDrop(Component c, FileDropListener listener) {
        c.setDropTarget(new DropTarget(c, DnDConstants.ACTION_COPY,
            new DropTargetAdapter() {
                @Override
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>)
                            dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        listener.filesDropped(files.toArray(new File[0]));
                        dtde.dropComplete(true);
                    } catch (Exception e) {
                        dtde.rejectDrop();
                    }
                }
            }, true));
    }
}
