package place.sita.labelle.core.images.imagelocator;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.RootRepository;
import place.sita.labelle.core.utils.Result2;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.UUID;

@Component
public class ImageLocatorService {

    private final DSLContext context;
    private final RootRepository rootRepository;

    public ImageLocatorService(DSLContext context, RootRepository rootRepository) {
        this.context = context;
        this.rootRepository = rootRepository;
    }

    public List<Root> roots() {
        return rootRepository.getRoots();
    }

    public Result2<Root, RootRepository.BaseOfRootViolation> createRoot(String text) {
        return rootRepository.createRoot(text);
    }

    public Result2<Void, RootRepository.RemovalNotPossibleDueToConstraints> removeRoot(UUID id) {
        return rootRepository.remove(id);
    }

    public BufferedImage load(ImagePtr imagePtr) {
        return null;
    }
}
